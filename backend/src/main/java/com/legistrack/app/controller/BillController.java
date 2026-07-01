package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.legistrack.app.service.BillEmbeddingService;
import com.legistrack.app.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Validated
@RequestMapping("/api/bills")
public class BillController {
    private final BillService billService;
    private final BillEmbeddingService embeddingService;

    public BillController(BillService billService, BillEmbeddingService embeddingService) {
        this.billService = billService;
        this.embeddingService = embeddingService;
    }

    @GetMapping("/search")
    public ResponseEntity<JsonNode> search(
            @RequestParam("q") String q,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "chamber", required = false) String chamber,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "committee", required = false) String committee) throws IOException {
        return ResponseEntity.ok(billService.search(q, year, chamber, status, committee));
    }

    @GetMapping("/semantic-search")
    public ResponseEntity<JsonNode> semanticSearch(@RequestParam("q") String q) throws IOException {
        return ResponseEntity.ok(billService.semanticSearch(q));
    }

    @PostMapping("/backfill-embeddings")
    public ResponseEntity<String> backfillEmbeddings() {
        embeddingService.backfillEmbeddings();
        return ResponseEntity.ok("{\"message\":\"Backfill started\"}");
    }

    @GetMapping("/{year}/{billId}")
    public ResponseEntity<JsonNode> get(@PathVariable("year") String year,
                                        @PathVariable("billId") String billId) throws IOException {
        return ResponseEntity.ok(billService.getBill(year, billId));
    }
}


