package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.legistrack.app.dto.MessageDto;
import com.legistrack.app.dto.SearchResponseDto;
import com.legistrack.app.dto.SemanticSearchResponseDto;
import com.legistrack.app.service.BillEmbeddingService;
import com.legistrack.app.service.BillService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
public class BillController {
    private final BillService billService;
    private final BillEmbeddingService embeddingService;

    public BillController(BillService billService, BillEmbeddingService embeddingService) {
        this.billService = billService;
        this.embeddingService = embeddingService;
    }

    @GetMapping("/search")
    public SearchResponseDto search(@RequestParam("q") String q,
                                    @RequestParam(value = "year", required = false) String year,
                                    @RequestParam(value = "chamber", required = false) String chamber,
                                    @RequestParam(value = "status", required = false) String status,
                                    @RequestParam(value = "committee", required = false) String committee) {
        return billService.search(q, year, chamber, status, committee);
    }

    @GetMapping("/semantic-search")
    public SemanticSearchResponseDto semanticSearch(@RequestParam("q") String q) {
        return billService.semanticSearch(q);
    }

    @PostMapping("/backfill-embeddings")
    public MessageDto backfillEmbeddings() {
        embeddingService.backfillEmbeddings();
        return new MessageDto("Backfill started");
    }

    @GetMapping("/{year}/{billId}")
    public JsonNode get(@PathVariable("year") String year, @PathVariable("billId") String billId) {
        if (!year.matches("\\d{4}")) {
            throw new IllegalArgumentException("year must be a 4-digit number");
        }
        return billService.getBill(year, billId);
    }
}
