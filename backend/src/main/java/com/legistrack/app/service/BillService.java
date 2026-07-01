package com.legistrack.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.legistrack.app.model.Bill;
import com.legistrack.app.repository.BillRepository;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class BillService {
    private final String baseUrl;
    private final String apiKey;
    private final CacheService cacheService;
    private final ObjectMapper mapper;
    private final BillRepository billRepository;
    private final BillEmbeddingService embeddingService;

    public BillService(@Value("${app.nysenate.baseUrl}") String baseUrl,
                       @Value("${app.nysenate.apiKey}") String apiKey,
                       CacheService cacheService,
                       BillRepository billRepository,
                       ObjectMapper mapper,
                       BillEmbeddingService embeddingService) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.cacheService = cacheService;
        this.billRepository = billRepository;
        this.mapper = mapper;
        this.embeddingService = embeddingService;
    }

    public JsonNode search(String query, String year, String chamber, String status, String committee) throws IOException {
        Integer yearInt = parseYear(year);
        // Only a validated year ever reaches the external API; anything invalid is dropped.
        String apiYear = yearInt != null ? String.valueOf(yearInt) : null;

        // API results (cached when no extra filters)
        String cacheKey = "search:" + query + ":" + (apiYear == null ? "" : apiYear);
        JsonNode apiNode;
        if (chamber == null && status == null && committee == null) {
            Optional<String> cached = cacheService.get(cacheKey);
            if (cached.isPresent()) {
                apiNode = mapper.readTree(cached.get());
            } else {
                apiNode = searchBillsFromApi(query, apiYear);
                cacheService.set(cacheKey, apiNode.toString(), Duration.ofMinutes(30));
            }
        } else {
            apiNode = searchBillsFromApi(query, apiYear);
        }

        ArrayNode apiResults = mapper.createArrayNode();
        JsonNode items = apiNode.path("result").path("items");
        if (items.isArray()) {
            items.forEach(item -> {
                JsonNode r = item.path("result");
                apiResults.add(r.isMissingNode() ? item : r);
            });
        }

        // Local DB results
        List<Bill> localBills = billRepository.searchBills(query, yearInt, chamber, status, committee);
        ArrayNode localResults = mapper.createArrayNode();
        for (Bill b : localBills) {
            localResults.add(billToNode(b));
        }

        ObjectNode response = mapper.createObjectNode();
        response.set("apiResults", apiResults);
        response.set("localResults", localResults);
        return response;
    }

    public JsonNode semanticSearch(String query) throws IOException {
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        String embeddingStr = embeddingService.floatArrayToString(queryEmbedding);
        List<Bill> similar = billRepository.findSimilarBills(embeddingStr, 20);
        ArrayNode results = mapper.createArrayNode();
        for (Bill b : similar) {
            results.add(billToNode(b));
        }
        ObjectNode response = mapper.createObjectNode();
        response.set("results", results);
        return response;
    }

    private ObjectNode billToNode(Bill b) {
        ObjectNode node = mapper.createObjectNode();
        node.put("basePrintNoStr", b.getBasePrintNoStr());
        node.put("title", b.getTitle());
        if (b.getSummary() != null) node.put("summary", b.getSummary());
        node.put("chamber", b.getChamber());
        if (b.getYear() != null) node.put("year", b.getYear());
        if (b.getStatus() != null) node.put("status", b.getStatus());
        if (b.getSponsorName() != null) node.put("sponsorName", b.getSponsorName());
        if (b.getCommitteeName() != null) node.put("committeeName", b.getCommitteeName());
        return node;
    }

    private Integer parseYear(String year) {
        if (year == null || year.isBlank()) return null;
        try {
            int y = Integer.parseInt(year.trim());
            return (y >= 2010 && y <= 2030) ? y : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public JsonNode getBill(String year, String billId) throws IOException {
        String basePrintNoStr = billId + "-" + year;

        Optional<Bill> existingBill = billRepository.findByBasePrintNoStr(basePrintNoStr);
        if (existingBill.isPresent()) {
            ObjectNode wrapper = mapper.createObjectNode();
            wrapper.set("result", mapper.valueToTree(existingBill.get()));
            return wrapper;
        }
 
        String cacheKey = "bill:" + basePrintNoStr;
        JsonNode apiResult = getBillFromApi(year, billId);
        cacheService.set(cacheKey, apiResult.toString(), Duration.ofHours(24));

        try {
            Bill bill = parseBillFromApi(apiResult);
            Bill saved = billRepository.save(bill);
            embeddingService.generateAndStoreEmbedding(saved);
        } catch (Exception e) {
            System.err.println("Failed to save bill to DB: " + e.getMessage());
        }

        return apiResult;
    }

    private Bill parseBillFromApi(JsonNode apiResult) {
        JsonNode billData = apiResult.path("result");
        
        String basePrintNoStr = billData.path("basePrintNoStr").asText();
        String title = billData.path("title").asText();
        String summary = billData.path("summary").asText();
        
        String memo = "";
        JsonNode amendments = billData.path("amendments").path("items");
        if (amendments.isArray() && amendments.size() > 0) {
            memo = amendments.get(0).path("memo").asText();
        }
        
        String chamber = basePrintNoStr.startsWith("S") ? "Senate" : "Assembly";
        Integer year = billData.path("session").asInt();
        String sponsorName = billData.path("sponsor").path("member").path("fullName").asText();
        String status = billData.path("status").path("statusDesc").asText();
        String committeeName = billData.path("status").path("committeeName").asText(null);

        OffsetDateTime publishedDate = null;
        String publishedDateStr = billData.path("publishedDateTime").asText();
        if (!publishedDateStr.isEmpty()) {
            try {
                publishedDate = OffsetDateTime.parse(publishedDateStr);
            } catch (Exception e) {
            }
        }

        Bill bill = new Bill(basePrintNoStr, title, summary, memo, chamber, year,
                             sponsorName, status, publishedDate);
        bill.setCommitteeName(committeeName);
        return bill;
    }

    private JsonNode searchBillsFromApi(String query, String year) throws IOException {
        String url = baseUrl + "bills/search?term=" + encode(query) + (year != null ? "&year=" + encode(year) : "") + "&key=" + apiKey;
        return getJson(url);
    }

    /**
     * Fetch fresh bill status directly from NY Senate API, bypassing cache and DB.
     * Used by the scheduler to detect status changes.
     */
    public String fetchFreshBillStatus(String year, String billId) throws IOException {
        JsonNode apiResult = getBillFromApi(year, billId);
        JsonNode result = apiResult.path("result");
        JsonNode statusNode = result.path("status").path("statusDesc");
        if (!statusNode.isMissingNode()) {
            return statusNode.asText();
        }
        return null;
    }

    private JsonNode getBillFromApi(String year, String billId) throws IOException {
        String url = baseUrl + "bills/" + encode(year) + "/" + encode(billId) + "?key=" + apiKey + "&view=with_refs";
        return getJson(url);
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private JsonNode getJson(String url) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            return client.execute(get, response -> {
                String body = EntityUtils.toString(response.getEntity());
                return mapper.readTree(body);
            });
        }
    }
}


