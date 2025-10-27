package com.legistrack.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

@Service
public class AiService {
    private final String apiKey;
    private final CacheService cacheService;
    private final ObjectMapper mapper = new ObjectMapper();

    public AiService(@Value("${app.huggingface.apiKey}") String apiKey, CacheService cacheService) {
        this.apiKey = apiKey;
        this.cacheService = cacheService;
    }

    public String summarizeMemo(String basePrintNoStr, String memoText) throws IOException {
        String cacheKey = "ai:summary:" + basePrintNoStr;
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }
        String response = summarizeMemoFromApi(memoText);
        cacheService.set(cacheKey, response, Duration.ofDays(7));
        return response;
    }

    private String summarizeMemoFromApi(String memoText) throws IOException {
        String endpoint = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(endpoint);
            post.addHeader("Authorization", "Bearer " + apiKey);
            post.addHeader("Content-Type", "application/json");
            String body = "{\"inputs\": " + mapper.writeValueAsString(memoText) + "}";
            post.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(body));
            return client.execute(post, response -> {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (responseBody.contains("\"error\"") || responseBody.contains("index out of range"))
                    throw new IOException("INSUFFICIENT_CONTENT");
                return responseBody;
            });
        }
    }
}


