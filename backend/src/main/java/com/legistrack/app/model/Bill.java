package com.legistrack.app.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "bills")
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "base_print_no_str", unique = true, nullable = false)
    private String basePrintNoStr;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "summary")
    private String summary;
    
    @Column(name = "memo")
    private String memo;
    
    @Column(name = "chamber", nullable = false)
    private String chamber;
    
    @Column(name = "year")
    private Integer year;
    
    @Column(name = "sponsor_name")
    private String sponsorName;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "published_date")
    private OffsetDateTime publishedDate;
    
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    @Column(name = "content_embedding")
    private float[] contentEmbedding;
    
    public Bill() {}
    
    public Bill(String basePrintNoStr, String title, String summary, String memo, 
                String chamber, Integer year, String sponsorName, String status, OffsetDateTime publishedDate) {
        this.basePrintNoStr = basePrintNoStr;
        this.title = title;
        this.summary = summary;
        this.memo = memo;
        this.chamber = chamber;
        this.year = year;
        this.sponsorName = sponsorName;
        this.status = status;
        this.publishedDate = publishedDate;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        this.contentEmbedding = new float[768];
    }
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getBasePrintNoStr() { return basePrintNoStr; }
    public void setBasePrintNoStr(String basePrintNoStr) { this.basePrintNoStr = basePrintNoStr; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getMemo() { return memo; }
    public void setMemo(String memo) { this.memo = memo; }
    
    public String getChamber() { return chamber; }
    public void setChamber(String chamber) { this.chamber = chamber; }
    
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    
    public String getSponsorName() { return sponsorName; }
    public void setSponsorName(String sponsorName) { this.sponsorName = sponsorName; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public OffsetDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(OffsetDateTime publishedDate) { this.publishedDate = publishedDate; }
    
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }

    public float[] getContentEmbedding() { return contentEmbedding; }
    public void setContentEmbedding(float[] contentEmbedding) { this.contentEmbedding = contentEmbedding; }
}
