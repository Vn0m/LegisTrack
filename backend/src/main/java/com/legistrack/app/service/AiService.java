package com.legistrack.app.service;

import com.legistrack.app.exception.UpstreamServiceException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class AiService {
    private static final String SUMMARIZATION_PATH = "/hf-inference/models/facebook/bart-large-cnn";
    private static final Duration SUMMARY_CACHE_TTL = Duration.ofDays(7);
    // bart-large-cnn takes ~1024 tokens of input; anything beyond gets ignored.
    private static final int MAX_INPUT_CHARS = 4000;

    private final RestClient huggingFace;
    private final CacheService cacheService;

    public AiService(@Qualifier("huggingFaceRestClient") RestClient huggingFace,
                     CacheService cacheService) {
        this.huggingFace = huggingFace;
        this.cacheService = cacheService;
    }

    public String summarize(String basePrintNoStr, String text) {
        String cacheKey = "ai:summary:" + basePrintNoStr;
        Optional<String> cached = cacheService.get(cacheKey);
        if (cached.isPresent()) {
            return cached.get();
        }

        String input = text.length() > MAX_INPUT_CHARS ? text.substring(0, MAX_INPUT_CHARS) : text;
        String response = huggingFace.post()
            .uri(SUMMARIZATION_PATH)
            .contentType(MediaType.APPLICATION_JSON)
            .body(Map.of("inputs", input))
            .retrieve()
            .body(String.class);
        if (response == null || response.contains("\"error\"")) {
            throw new UpstreamServiceException("AI summarization failed");
        }

        cacheService.set(cacheKey, response, SUMMARY_CACHE_TTL);
        return response;
    }
}
