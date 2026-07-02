package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.legistrack.app.dto.SummarizeRequest;
import com.legistrack.app.service.AiService;
import com.legistrack.app.service.BillService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private static final int MIN_SUMMARIZABLE_CHARS = 100;

    private final AiService aiService;
    private final BillService billService;

    public AiController(AiService aiService, BillService billService) {
        this.aiService = aiService;
        this.billService = billService;
    }

    @PostMapping(value = "/summarize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> summarize(@RequestBody SummarizeRequest request) {
        String basePrintNoStr = request.basePrintNoStr();
        if (basePrintNoStr == null || !basePrintNoStr.matches("[A-Za-z]\\d+[A-Za-z]?-\\d{4}")) {
            throw new IllegalArgumentException("Missing or invalid basePrintNoStr");
        }
        int dash = basePrintNoStr.lastIndexOf('-');
        String billId = basePrintNoStr.substring(0, dash);
        String year = basePrintNoStr.substring(dash + 1);

        String text = extractSummarizableText(billService.getBill(year, billId));
        if (text.isBlank()) {
            throw new IllegalArgumentException("No memo or summary found for bill");
        }
        if (text.length() < MIN_SUMMARIZABLE_CHARS) {
            throw new IllegalArgumentException(
                "This bill does not have enough content for AI summarization");
        }

        // The HuggingFace payload ([{"summary_text": ...}]) is passed through as-is.
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(aiService.summarize(basePrintNoStr, text));
    }

    private String extractSummarizableText(JsonNode bill) {
        JsonNode result = bill.path("result");
        String memo = result.path("amendments").path("items")
            .path(result.path("activeVersion").asText(""))
            .path("memo").asText("");
        if (!memo.isBlank()) {
            return memo;
        }
        return result.path("summary").asText("");
    }
}
