package com.legistrack.app.service;

import com.legistrack.app.model.Bill;
import com.legistrack.app.model.BillLabel;
import com.legistrack.app.model.Label;
import com.legistrack.app.repository.BillLabelRepository;
import com.legistrack.app.repository.BillRepository;
import com.legistrack.app.repository.LabelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LabelService {
    private final LabelRepository labelRepository;
    private final BillLabelRepository billLabelRepository;
    private final BillRepository billRepository;

    public LabelService(LabelRepository labelRepository,
                        BillLabelRepository billLabelRepository,
                        BillRepository billRepository) {
        this.labelRepository = labelRepository;
        this.billLabelRepository = billLabelRepository;
        this.billRepository = billRepository;
    }

    public List<Label> getAllLabels() {
        return labelRepository.findAll();
    }

    @Transactional
    public Label createLabel(String labelText) {
        if (labelRepository.existsByLabel(labelText)) {
            throw new IllegalArgumentException("Label already exists: " + labelText);
        }
        return labelRepository.save(new Label(labelText));
    }

    @Transactional
    public void deleteLabel(UUID labelId) {
        if (!labelRepository.existsById(labelId)) {
            throw new IllegalArgumentException("Label not found");
        }
        labelRepository.deleteById(labelId);
    }

    public List<BillLabel> getLabelsForBill(String basePrintNoStr) {
        return billRepository.findByBasePrintNoStr(basePrintNoStr)
            .map(bill -> billLabelRepository.findByBill_Id(bill.getId()))
            .orElseGet(List::of);
    }

    @Transactional
    public BillLabel addLabelToBill(String basePrintNoStr, UUID labelId) {
        Bill bill = billRepository.findByBasePrintNoStr(basePrintNoStr)
            .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + basePrintNoStr));
        Label label = labelRepository.findById(labelId)
            .orElseThrow(() -> new IllegalArgumentException("Label not found: " + labelId));
        if (billLabelRepository.existsByBill_IdAndLabel_Id(bill.getId(), labelId)) {
            throw new IllegalStateException("Label already applied to this bill");
        }
        return billLabelRepository.save(new BillLabel(bill, label));
    }

    @Transactional
    public void removeLabelFromBill(String basePrintNoStr, UUID labelId) {
        Bill bill = billRepository.findByBasePrintNoStr(basePrintNoStr)
            .orElseThrow(() -> new IllegalArgumentException("Bill not found: " + basePrintNoStr));
        billLabelRepository.deleteByBill_IdAndLabel_Id(bill.getId(), labelId);
    }
}
