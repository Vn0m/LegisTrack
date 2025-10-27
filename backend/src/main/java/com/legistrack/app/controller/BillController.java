package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.legistrack.app.service.BillService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@Validated
@RequestMapping("/api/bills")
public class BillController {
    private final BillService billService;

    public BillController(BillService billService) {
        this.billService = billService;
    }

    @GetMapping("/search")
    public ResponseEntity<JsonNode> search(@RequestParam("q") String q,
                                           @RequestParam(value = "year", required = false) String year) throws IOException {
        return ResponseEntity.ok(billService.search(q, year));
    }

    @GetMapping("/{year}/{billId}")
    public ResponseEntity<JsonNode> get(@PathVariable("year") String year,
                                        @PathVariable("billId") String billId) throws IOException {
        return ResponseEntity.ok(billService.getBill(year, billId));
    }
}


