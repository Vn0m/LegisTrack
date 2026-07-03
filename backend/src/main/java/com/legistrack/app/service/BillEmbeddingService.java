package com.legistrack.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.legistrack.app.exception.UpstreamServiceException;
import com.legistrack.app.model.Bill;
import com.legistrack.app.repository.BillRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class BillEmbeddingService {
    private static final Logger logger = LoggerFactory.getLogger(BillEmbeddingService.class);

    private static final String EMBEDDING_PATH =
        "/hf-inference/models/sentence-transformers/all-mpnet-base-v2/pipeline/feature-extraction";
    private static final int EMBEDDING_DIMENSIONS = 768;
    private static final int MAX_INPUT_CHARS = 2000;

    private final RestClient huggingFace;
    private final BillRepository billRepository;

    public BillEmbeddingService(@Qualifier("huggingFaceRestClient") RestClient huggingFace,
                                BillRepository billRepository) {
        this.huggingFace = huggingFace;
        this.billRepository = billRepository;
    }

    @Async
    public void generateAndStoreEmbedding(Bill bill) {
        embed(bill);
    }

    @Async
    public void backfillEmbeddings() {
        List<Bill> bills = billRepository.findBillsWithoutEmbeddings();
        logger.info("Backfilling embeddings for {} bills", bills.size());
        int stored = 0;
        for (Bill bill : bills) {
            if (embed(bill)) stored++;
        }
        logger.info("Backfill finished: {}/{} embeddings stored", stored, bills.size());
    }

    public float[] generateEmbedding(String text) {
        JsonNode response = huggingFace.post()
            .uri(EMBEDDING_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("inputs", text))
            .retrieve()
            .body(JsonNode.class);
        if (response == null) {
            throw new UpstreamServiceException("Empty embedding response from HuggingFace");
        }
        JsonNode vector = response.isArray() && response.size() > 0 && response.get(0).isArray()
            ? response.get(0)
            : response;
        if (!vector.isArray() || vector.size() != EMBEDDING_DIMENSIONS) {
            throw new UpstreamServiceException("Unexpected embedding response shape from HuggingFace");
        }
        float[] result = new float[EMBEDDING_DIMENSIONS];
        for (int i = 0; i < EMBEDDING_DIMENSIONS; i++) {
            result[i] = (float) vector.get(i).asDouble();
        }
        return result;
    }

    public static String toVectorLiteral(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(embedding[i]);
        }
        return sb.append(']').toString();
    }

    private boolean embed(Bill bill) {
        try {
            String text = buildEmbeddingText(bill);
            if (text.isBlank()) {
                return false;
            }
            float[] embedding = generateEmbedding(text);
            billRepository.updateEmbedding(bill.getId(), toVectorLiteral(embedding));
            return true;
        } catch (Exception e) {
            logger.warn("Embedding failed for {}: {}", bill.getBasePrintNoStr(), e.getMessage());
            return false;
        }
    }

    private String buildEmbeddingText(Bill bill) {
        StringBuilder sb = new StringBuilder();
        if (bill.getTitle() != null) sb.append(bill.getTitle()).append(". ");
        if (bill.getSummary() != null && !bill.getSummary().isBlank()) sb.append(bill.getSummary()).append(' ');
        if (bill.getMemo() != null && !bill.getMemo().isBlank()) sb.append(bill.getMemo());
        String text = sb.toString().trim();
        return text.length() > MAX_INPUT_CHARS ? text.substring(0, MAX_INPUT_CHARS) : text;
    }
}
