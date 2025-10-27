package com.legistrack.app.model;

import java.util.UUID;
import java.util.Objects;

public class BillLabelId implements java.io.Serializable {
    private UUID bill;
    private UUID label;
    
    public BillLabelId() {}
    
    public BillLabelId(UUID bill, UUID label) {
        this.bill = bill;
        this.label = label;
    }
    
    public UUID getBill() { return bill; }
    public void setBill(UUID bill) { this.bill = bill; }
    
    public UUID getLabel() { return label; }
    public void setLabel(UUID label) { this.label = label; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillLabelId that = (BillLabelId) o;
        return Objects.equals(bill, that.bill) && Objects.equals(label, that.label);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(bill, label);
    }
}
