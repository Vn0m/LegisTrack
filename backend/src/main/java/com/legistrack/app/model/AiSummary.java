package com.legistrack.app.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_summaries")
public class AiSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;
    
    @Column(name = "summary_text", nullable = false)
    private String summaryText;
    
    @Column(name = "key_points", columnDefinition = "jsonb")
    private String keyPoints; 
    
    @Column(name = "model_used")
    private String modelUsed;
    
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    
    public AiSummary() {}
    
    public AiSummary(Bill bill, String summaryText, String keyPoints, String modelUsed) {
        this.bill = bill;
        this.summaryText = summaryText;
        this.keyPoints = keyPoints;
        this.modelUsed = modelUsed;
        this.createdAt = OffsetDateTime.now();
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }
    
    public String getSummaryText() { return summaryText; }
    public void setSummaryText(String summaryText) { this.summaryText = summaryText; }
    
    public String getKeyPoints() { return keyPoints; }
    public void setKeyPoints(String keyPoints) { this.keyPoints = keyPoints; }
    
    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
