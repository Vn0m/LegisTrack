package com.legistrack.app.service;

import com.legistrack.app.dto.LabelDto;
import com.legistrack.app.dto.SavedBillDto;
import com.legistrack.app.dto.SavedBillsResponseDto;
import com.legistrack.app.dto.SavedStatusDto;
import com.legistrack.app.exception.NotFoundException;
import com.legistrack.app.model.Bill;
import com.legistrack.app.model.SavedBill;
import com.legistrack.app.repository.BillLabelRepository;
import com.legistrack.app.repository.BillRepository;
import com.legistrack.app.repository.SavedBillRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SavedBillService {

    private final BillRepository billRepository;
    private final SavedBillRepository savedBillRepository;
    private final BillLabelRepository billLabelRepository;

    public SavedBillService(BillRepository billRepository,
                            SavedBillRepository savedBillRepository,
                            BillLabelRepository billLabelRepository) {
        this.billRepository = billRepository;
        this.savedBillRepository = savedBillRepository;
        this.billLabelRepository = billLabelRepository;
    }

    @Transactional
    public void saveBill(UUID userId, String basePrintNoStr, String notes) {
        // Every bill a user can see has already been ingested by search or the
        // detail view, so saving never needs to create placeholder rows.
        Bill bill = requireBill(basePrintNoStr);
        if (savedBillRepository.existsByUserIdAndBill_Id(userId, bill.getId())) {
            throw new IllegalStateException("Bill already saved");
        }
        try {
            savedBillRepository.save(new SavedBill(userId, bill, notes));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Bill already saved");
        }
    }

    @Transactional
    public void unsaveBill(UUID userId, String basePrintNoStr) {
        Bill bill = requireBill(basePrintNoStr);
        savedBillRepository.deleteByUserIdAndBill_Id(userId, bill.getId());
    }

    public SavedBillsResponseDto getUserSavedBills(UUID userId) {
        List<SavedBill> saved = savedBillRepository.findByUserIdWithBillDetails(userId);
        List<UUID> billIds = saved.stream().map(sb -> sb.getBill().getId()).toList();
        Map<UUID, List<LabelDto>> labelsByBill = billIds.isEmpty() ? Map.of()
            : billLabelRepository.findByBill_IdIn(billIds).stream()
                .collect(Collectors.groupingBy(bl -> bl.getBill().getId(),
                    Collectors.mapping(bl -> LabelDto.from(bl.getLabel()), Collectors.toList())));

        List<SavedBillDto> bills = saved.stream().map(sb -> new SavedBillDto(
            sb.getId(),
            sb.getBill().getBasePrintNoStr(),
            sb.getBill().getTitle(),
            sb.getSavedAt(),
            sb.getNotes() != null ? sb.getNotes() : "",
            labelsByBill.getOrDefault(sb.getBill().getId(), List.of())
        )).toList();
        return new SavedBillsResponseDto(bills);
    }

    public SavedStatusDto getSavedStatus(UUID userId, String basePrintNoStr) {
        return billRepository.findByBasePrintNoStr(basePrintNoStr)
            .flatMap(bill -> savedBillRepository.findByUserIdAndBill_Id(userId, bill.getId()))
            .map(sb -> new SavedStatusDto(true, sb.getNotes() != null ? sb.getNotes() : ""))
            .orElseGet(() -> new SavedStatusDto(false, ""));
    }

    @Transactional
    public void updateNotes(UUID userId, String basePrintNoStr, String notes) {
        Bill bill = requireBill(basePrintNoStr);
        SavedBill savedBill = savedBillRepository.findByUserIdAndBill_Id(userId, bill.getId())
            .orElseThrow(() -> new NotFoundException("Saved bill not found"));
        savedBill.setNotes(notes);
        savedBillRepository.save(savedBill);
    }

    private Bill requireBill(String basePrintNoStr) {
        if (basePrintNoStr == null || basePrintNoStr.isBlank()) {
            throw new IllegalArgumentException("basePrintNoStr is required");
        }
        return billRepository.findByBasePrintNoStr(basePrintNoStr)
            .orElseThrow(() -> new NotFoundException("Bill not found: " + basePrintNoStr));
    }
}
