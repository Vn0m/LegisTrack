package com.legistrack.app.controller;

import com.legistrack.app.model.SavedBill;
import com.legistrack.app.service.SavedBillService;
import com.legistrack.app.service.SupabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved-bills")
@CrossOrigin(origins = "*")
public class SavedBillController {

    @Autowired
    private SupabaseService supabaseService;
    
    @Autowired
    private SavedBillService savedBillService;

    @PostMapping("/save")
    public ResponseEntity<String> saveBill(@RequestHeader("Authorization") String authToken, 
                                         @RequestBody Map<String, Object> body) {
        try {
            UUID userId = supabaseService.validateUser(authToken);
            if (userId == null) {
                return ResponseEntity.status(401).body("{\"error\": \"Unauthorized\"}");
            }

            savedBillService.saveBill(userId, body);
            return ResponseEntity.ok("{\"message\": \"Bill saved successfully\"}");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (IllegalStateException e) {
            return ResponseEntity.ok("{\"message\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @DeleteMapping("/unsave")
    public ResponseEntity<String> unsaveBill(@RequestHeader("Authorization") String authToken,
                                            @RequestBody Map<String, Object> body) {
        try {
            UUID userId = supabaseService.validateUser(authToken);
            if (userId == null) {
                return ResponseEntity.status(401).body("{\"error\": \"Unauthorized\"}");
            }

            String basePrintNoStr = body.get("basePrintNoStr") != null ? body.get("basePrintNoStr").toString() : null;
            savedBillService.unsaveBill(userId, basePrintNoStr);
            
            return ResponseEntity.ok("{\"message\": \"Bill unsaved successfully\"}");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/my-bills")
    public ResponseEntity<String> getMyBills(@RequestHeader("Authorization") String authToken) {
        try {
            UUID userId = supabaseService.validateUser(authToken);
            if (userId == null) {
                return ResponseEntity.status(401).body("{\"error\": \"Unauthorized\"}");
            }

            List<SavedBill> savedBills = savedBillService.getUserSavedBills(userId);
            
            String billsJson = savedBills.stream()
                .map(sb -> String.format(
                    "{\"id\":\"%s\",\"basePrintNoStr\":\"%s\",\"title\":\"%s\",\"savedAt\":\"%s\",\"notes\":\"%s\"}",
                    sb.getId(),
                    sb.getBill().getBasePrintNoStr(),
                    sb.getBill().getTitle(),
                    sb.getSavedAt(),
                    sb.getNotes() != null ? sb.getNotes() : ""
                ))
                .collect(Collectors.joining(","));
            
            return ResponseEntity.ok("{\"bills\": [" + billsJson + "]}");
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
