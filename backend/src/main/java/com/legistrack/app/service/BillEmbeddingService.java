package com.legistrack.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.legistrack.app.model.Bill;
import com.legistrack.app.repository.BillRepository;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class BillEmbeddingService {

    private static final String EMBEDDING_MODEL = "sentence-transformers/all-mpnet-base-v2";

    private final String apiKey;
    private final ObjectMapper mapper;
    private final BillRepository billRepository;

    public BillEmbeddingService(@Value("${app.huggingface.apiKey}") String apiKey,
                                ObjectMapper mapper,
                                BillRepository billRepository) {
        this.apiKey = apiKey;
        this.mapper = mapper;
        this.billRepository = billRepository;
    }

    @Async
    public void generateAndStoreEmbedding(Bill bill) {
        try {
            String text = buildEmbeddingText(bill);
            float[] embedding = generateEmbedding(text);
            billRepository.updateEmbedding(bill.getId().toString(), floatArrayToString(embedding));
        } catch (Exception e) {
            System.err.println("Failed to generate embedding for " + bill.getBasePrintNoStr() + ": " + e.getMessage());
        }
    }

    public float[] generateEmbedding(String text) throws IOException {
        String endpoint = "https://api-inference.huggingface.co/models/" + EMBEDDING_MODEL;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(endpoint);
            post.addHeader("Authorization", "Bearer " + apiKey);
            post.addHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity("{\"inputs\": " + mapper.writeValueAsString(text) + "}"));
            return client.execute(post, response -> {
                String body = EntityUtils.toString(response.getEntity());
                JsonNode json = mapper.readTree(body);
                if (json.has("error")) {
                    throw new IOException("HuggingFace error: " + json.path("error").asText());
                }
                JsonNode arr = json.isArray() ? json.get(0) : json;
                float[] result = new float[arr.size()];
                for (int i = 0; i < arr.size(); i++) {
                    result[i] = (float) arr.get(i).asDouble();
                }
                return result;
            });
        }
    }

    public void backfillEmbeddings() {
        List<Bill> bills = billRepository.findBillsWithoutEmbeddings();
        for (Bill bill : bills) {
            generateAndStoreEmbedding(bill);
        }
    }

    private String buildEmbeddingText(Bill bill) {
        StringBuilder sb = new StringBuilder();
        if (bill.getTitle() != null) sb.append(bill.getTitle()).append(". ");
        if (bill.getSummary() != null && !bill.getSummary().isBlank()) sb.append(bill.getSummary()).append(" ");
        if (bill.getMemo() != null && !bill.getMemo().isBlank()) {
            String memo = bill.getMemo();
            if (memo.length() > 512) memo = memo.substring(0, 512);
            sb.append(memo);
        }
        return sb.toString().trim();
    }

    public String floatArrayToString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
