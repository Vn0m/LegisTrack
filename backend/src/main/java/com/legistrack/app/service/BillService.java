package com.legistrack.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

@Service
@SuppressWarnings("null")
public class BillService {
    private final String baseUrl;
    private final String apiKey;
    private final CacheService cacheService;
    private final ObjectMapper mapper;
    private final BillRepository billRepository;

    public BillService(@Value("${app.nysenate.baseUrl}") String baseUrl,
                       @Value("${app.nysenate.apiKey}") String apiKey,
                       CacheService cacheService,
                       BillRepository billRepository,
                       ObjectMapper mapper) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.cacheService = cacheService;
        this.billRepository = billRepository;
        this.mapper = mapper;
    }

    public JsonNode search(String query, String year) throws IOException {
        String cacheKey = "search:" + query + ":" + (year == null ? "" : year);
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return mapper.readTree(cached.get());
        }
        JsonNode result = searchBills(query, year);
        cacheService.set(cacheKey, result.toString(), Duration.ofMinutes(30));
        return result;
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

        try{
            Bill bill = parseBillFromApi(apiResult);
            billRepository.save(bill);

        } catch(Exception e){
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
        
        OffsetDateTime publishedDate = null;
        String publishedDateStr = billData.path("publishedDateTime").asText();
        if (!publishedDateStr.isEmpty()) {
            try {
                publishedDate = OffsetDateTime.parse(publishedDateStr);
            } catch (Exception e) {
            }
        }
        
        return new Bill(basePrintNoStr, title, summary, memo, chamber, year, 
                        sponsorName, status, publishedDate);
    }

    private JsonNode searchBills(String query, String year) throws IOException {
        String url = baseUrl + "bills/search?term=" + encode(query) + (year != null ? "&year=" + encode(year) : "") + "&key=" + apiKey;
        return getJson(url);
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


