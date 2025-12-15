package com.legistrack.app.service;

import com.legistrack.app.model.Bill;
import com.legistrack.app.model.SavedBill;
import com.legistrack.app.repository.BillRepository;
import com.legistrack.app.repository.SavedBillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class SavedBillService {

    @Autowired
    private BillRepository billRepository;
    
    @Autowired
    private SavedBillRepository savedBillRepository;

    @Transactional
    public SavedBill saveBill(UUID userId, Map<String, Object> billData) {
        String basePrintNoStr = getString(billData, "basePrintNoStr");
        String notes = getString(billData, "notes");
        
        if (basePrintNoStr == null) {
            throw new IllegalArgumentException("basePrintNoStr is required");
        }
        
        try {
            Bill bill = billRepository.findByBasePrintNoStr(basePrintNoStr)
                .orElseGet(() -> {
                    Bill newBill = new Bill();
                    newBill.setBasePrintNoStr(basePrintNoStr);
                    newBill.setTitle(getString(billData, "title", "Placeholder - To be fetched"));
                    newBill.setSummary(getString(billData, "summary"));
                    newBill.setMemo(getString(billData, "memo"));
                    newBill.setChamber(getString(billData, "chamber", "UNKNOWN"));
                    newBill.setYear(getInteger(billData, "year"));
                    newBill.setSponsorName(getString(billData, "sponsorName"));
                    newBill.setStatus(getString(billData, "status"));
                    newBill.setPublishedDate(OffsetDateTime.now());
                    newBill.setCreatedAt(OffsetDateTime.now());
                    newBill.setUpdatedAt(OffsetDateTime.now());
                    newBill.setContentEmbedding(new float[768]);
                    try {
                        return billRepository.save(newBill);
                    } catch (DataIntegrityViolationException e) {
                        return billRepository.findByBasePrintNoStr(basePrintNoStr)
                            .orElseThrow(() -> new RuntimeException("Failed to create or retrieve bill"));
                    }
                });
            
            if (savedBillRepository.existsByUserIdAndBill_Id(userId, bill.getId())) {
                throw new IllegalStateException("Bill already saved");
            }
            
            SavedBill savedBill = new SavedBill(userId, bill, notes);
            return savedBillRepository.save(savedBill);
            
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Bill already saved");
        }
    }

    @Transactional
    public void unsaveBill(UUID userId, String basePrintNoStr) {
        if (basePrintNoStr == null) {
            throw new IllegalArgumentException("basePrintNoStr is required");
        }
        
        Optional<Bill> billOpt = billRepository.findByBasePrintNoStr(basePrintNoStr);
        if (!billOpt.isPresent()) {
            throw new IllegalArgumentException("Bill not found");
        }
        
        savedBillRepository.deleteByUserIdAndBill_Id(userId, billOpt.get().getId());
    }

    public List<SavedBill> getUserSavedBills(UUID userId) {
        return savedBillRepository.findByUserIdWithBillDetails(userId);
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private String getString(Map<String, Object> map, String key, String defaultValue) {
        String value = getString(map, key);
        return value != null ? value : defaultValue;
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

