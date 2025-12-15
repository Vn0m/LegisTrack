package com.legistrack.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.legistrack.app.model.SavedBill;
import com.legistrack.app.service.SavedBillService;
import com.legistrack.app.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved-bills")
@CrossOrigin(origins = "*")
public class SavedBillController {

    @Autowired
    private SupabaseService supabaseService;
    
    @Autowired
    private SavedBillService savedBillService;
    
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/save")
    public ResponseEntity<String> saveBill(@RequestHeader("Authorization") String authToken, 
                                         @RequestBody Map<String, Object> body) throws Exception {
        try {
            UUID userId = supabaseService.validateUser(authToken);
            if (userId == null) {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(401).body(objectMapper.writeValueAsString(error));
            }

            savedBillService.saveBill(userId, body);
            ObjectNode success = objectMapper.createObjectNode();
            success.put("message", "Bill saved successfully");
            return ResponseEntity.ok(objectMapper.writeValueAsString(success));
            
        } catch (IllegalArgumentException e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(objectMapper.writeValueAsString(error));
        } catch (IllegalStateException e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            return ResponseEntity.status(409).body(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(objectMapper.writeValueAsString(error));
        }
    }

    @DeleteMapping("/unsave")
    public ResponseEntity<String> unsaveBill(@RequestHeader("Authorization") String authToken,
                                            @RequestBody Map<String, Object> body) throws Exception {
        try {
            UUID userId = supabaseService.validateUser(authToken);
            if (userId == null) {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(401).body(objectMapper.writeValueAsString(error));
            }

            String basePrintNoStr = body.get("basePrintNoStr") != null ? body.get("basePrintNoStr").toString() : null;
            savedBillService.unsaveBill(userId, basePrintNoStr);
            
            ObjectNode success = objectMapper.createObjectNode();
            success.put("message", "Bill unsaved successfully");
            return ResponseEntity.ok(objectMapper.writeValueAsString(success));
            
        } catch (IllegalArgumentException e) {
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(objectMapper.writeValueAsString(error));
        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(objectMapper.writeValueAsString(error));
        }
    }

    @GetMapping("/my-bills")
    public ResponseEntity<String> getMyBills(@RequestHeader("Authorization") String authToken) throws Exception {
        try {
            UUID userId = supabaseService.validateUser(authToken);
            if (userId == null) {
                ObjectNode error = objectMapper.createObjectNode();
                error.put("error", "Unauthorized");
                return ResponseEntity.status(401).body(objectMapper.writeValueAsString(error));
            }

            List<SavedBill> savedBills = savedBillService.getUserSavedBills(userId);
            
            ArrayNode billsArray = objectMapper.createArrayNode();
            for (SavedBill sb : savedBills) {
                ObjectNode billNode = objectMapper.createObjectNode();
                billNode.put("id", sb.getId().toString());
                billNode.put("basePrintNoStr", sb.getBill().getBasePrintNoStr());
                billNode.put("title", sb.getBill().getTitle());
                billNode.put("savedAt", sb.getSavedAt().toString());
                billNode.put("notes", sb.getNotes() != null ? sb.getNotes() : "");
                billsArray.add(billNode);
            }
            
            ObjectNode response = objectMapper.createObjectNode();
            response.set("bills", billsArray);
            return ResponseEntity.ok(objectMapper.writeValueAsString(response));
            
        } catch (Exception e) {
            e.printStackTrace();
            ObjectNode error = objectMapper.createObjectNode();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(objectMapper.writeValueAsString(error));
        }
    }
}
