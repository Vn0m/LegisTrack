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
import java.util.UUID;

@Service
public class SupabaseService {

    @Value("${app.supabase.url}")
    private String supabaseUrl;

    @Value("${app.supabase.serviceKey}")
    private String serviceKey;

    private final ObjectMapper mapper = new ObjectMapper();

    public UUID validateUser(String authToken) throws IOException {
        if (authToken == null || !authToken.startsWith("Bearer ")) {
            return null;
        }

        String jwt = authToken.substring(7);
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(supabaseUrl + "/auth/v1/user");
            request.setHeader("Authorization", "Bearer " + jwt);
            request.setHeader("apikey", serviceKey);

            return client.execute(request, response -> {
                if (response.getCode() == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    JsonNode userData = mapper.readTree(responseBody);
                    String userId = userData.get("id").asText();
                    return UUID.fromString(userId);
                }
                return null;
            });
        }
    }
}
