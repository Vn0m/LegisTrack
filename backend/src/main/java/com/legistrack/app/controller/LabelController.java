package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.legistrack.app.model.BillLabel;
import com.legistrack.app.model.Label;
import com.legistrack.app.service.LabelService;
import com.legistrack.app.service.SupabaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/labels")
@CrossOrigin(origins = "*")
public class LabelController {
    private final LabelService labelService;
    private final SupabaseService supabaseService;
    private final ObjectMapper objectMapper;

    public LabelController(LabelService labelService,
                           SupabaseService supabaseService,
                           ObjectMapper objectMapper) {
        this.labelService = labelService;
        this.supabaseService = supabaseService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> getAllLabels() throws Exception {
        List<Label> labels = labelService.getAllLabels();
        ArrayNode arr = objectMapper.createArrayNode();
        for (Label l : labels) {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("id", l.getId() != null ? l.getId().toString() : "");
            node.put("label", l.getLabel());
            arr.add(node);
        }
        ObjectNode response = objectMapper.createObjectNode();
        response.set("labels", arr);
        return ResponseEntity.ok(objectMapper.writeValueAsString(response));
    }

    @PostMapping
    public ResponseEntity<String> createLabel(@RequestHeader("Authorization") String authToken,
                                              @RequestBody Map<String, String> body) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");
        try {
            Label label = labelService.createLabel(body.get("label"));
            ObjectNode response = objectMapper.createObjectNode();
            response.put("id", label.getId().toString());
            response.put("label", label.getLabel());
            return ResponseEntity.ok(objectMapper.writeValueAsString(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteLabel(@RequestHeader("Authorization") String authToken,
                                              @PathVariable UUID id) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");
        try {
            labelService.deleteLabel(id);
            return ResponseEntity.ok("{\"message\":\"Label deleted\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/bill/{basePrintNoStr}")
    public ResponseEntity<String> getLabelsForBill(@PathVariable String basePrintNoStr) throws Exception {
        try {
            List<BillLabel> billLabels = labelService.getLabelsForBill(basePrintNoStr);
            ArrayNode arr = objectMapper.createArrayNode();
            for (BillLabel bl : billLabels) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("id", bl.getLabel().getId().toString());
                node.put("label", bl.getLabel().getLabel());
                arr.add(node);
            }
            ObjectNode response = objectMapper.createObjectNode();
            response.set("labels", arr);
            return ResponseEntity.ok(objectMapper.writeValueAsString(response));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok("{\"labels\":[]}");
        }
    }

    @PostMapping("/bill/{basePrintNoStr}/{labelId}")
    public ResponseEntity<String> addLabelToBill(@RequestHeader("Authorization") String authToken,
                                                 @PathVariable String basePrintNoStr,
                                                 @PathVariable UUID labelId) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");
        try {
            labelService.addLabelToBill(basePrintNoStr, labelId);
            return ResponseEntity.ok("{\"message\":\"Label added\"}");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/bill/{basePrintNoStr}/{labelId}")
    public ResponseEntity<String> removeLabelFromBill(@RequestHeader("Authorization") String authToken,
                                                      @PathVariable String basePrintNoStr,
                                                      @PathVariable UUID labelId) throws Exception {
        UUID userId = supabaseService.validateUser(authToken);
        if (userId == null) return ResponseEntity.status(401).body("{\"error\":\"Unauthorized\"}");
        labelService.removeLabelFromBill(basePrintNoStr, labelId);
        return ResponseEntity.ok("{\"message\":\"Label removed\"}");
    }
}
