package com.legistrack.app.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "bill_labels")
@IdClass(BillLabelId.class)
public class BillLabel {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "label_id", nullable = false)
    private Label label;
    
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    
    public BillLabel() {}
    
    public BillLabel(Bill bill, Label label) {
        this.bill = bill;
        this.label = label;
        this.createdAt = OffsetDateTime.now();
    }
    
    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
    
    public Label getLabel() { return label; }
    public void setLabel(Label label) { this.label = label; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillLabel billLabel = (BillLabel) o;
        return Objects.equals(bill, billLabel.bill) && Objects.equals(label, billLabel.label);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bill, label);
    }
}

