package com.legistrack.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Service
public class BillService {
    private final String baseUrl;
    private final String apiKey;
    private final CacheService cacheService;
    private final ObjectMapper mapper = new ObjectMapper();

    public BillService(@Value("${app.nysenate.baseUrl}") String baseUrl,
                       @Value("${app.nysenate.apiKey}") String apiKey,
                       CacheService cacheService) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.cacheService = cacheService;
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
        String basePrintNoStr = billId + "-" + year; // e.g., S5665-2023
        String cacheKey = "bill:" + basePrintNoStr;
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return mapper.readTree(cached.get());
        }
        JsonNode result = getBillFromApi(year, billId);
        cacheService.set(cacheKey, result.toString(), Duration.ofHours(24));
        return result;
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


