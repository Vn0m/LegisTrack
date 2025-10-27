package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.legistrack.app.service.AiService;
import com.legistrack.app.service.BillService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;
    private final BillService billService;

    public AiController(AiService aiService, BillService billService) {
        this.aiService = aiService;
        this.billService = billService;
    }

    @PostMapping(value = "/summarize", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> summarize(@RequestBody Map<String, String> body) throws IOException {
        String basePrintNoStr = body.get("basePrintNoStr"); 
        if (basePrintNoStr == null || !basePrintNoStr.contains("-")) {
            return ResponseEntity.badRequest().body("Missing or invalid basePrintNoStr");
        }
        String[] parts = basePrintNoStr.split("-");
        String billId = parts[0];
        String year = parts[1];

        JsonNode bill = billService.getBill(year, billId);
        
        JsonNode amendments = bill.at("/result/amendments");
        String textToSummarize = "";
        
        if (!amendments.isMissingNode() && amendments.has("items")) {
            JsonNode items = amendments.get("items");
            if (items.isObject()) {
                JsonNode baseAmendment = items.get("");
                if (baseAmendment != null && baseAmendment.has("memo")) {
                    JsonNode memoNode = baseAmendment.get("memo");
                    if (memoNode != null && !memoNode.isNull() && !memoNode.asText("").isEmpty()) {
                        textToSummarize = memoNode.asText("");
                    }
                }
            }
        }
        
        if (textToSummarize.isBlank()) {
            JsonNode summaryNode = bill.at("/result/summary");
            if (!summaryNode.isMissingNode() && !summaryNode.isNull()) {
                textToSummarize = summaryNode.asText("");
            }
        }
        
        if (textToSummarize.isBlank()) {
            return ResponseEntity.badRequest().body("{\"error\": \"No memo or summary found for bill\"}");
        }
        
        if (textToSummarize.length() < 100) {
            return ResponseEntity.badRequest().body("{\"error\": \"Text too short for summarization\"}");
        }
        
        try {
            String response = aiService.summarizeMemo(basePrintNoStr, textToSummarize);
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(response);
        } catch (IOException e) {
            if (e.getMessage().equals("INSUFFICIENT_CONTENT")) {
                return ResponseEntity.status(400).body("{\"error\": \"This bill does not have enough content for AI summarization. Bills with minimal or missing detailed memos cannot be summarized.\"}");
            }
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage().replace("\"", "\\\"") + "\"}");
        }
    }
}


