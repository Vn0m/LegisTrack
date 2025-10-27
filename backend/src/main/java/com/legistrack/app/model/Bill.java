package com.legistrack.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
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
    private LocalDateTime publishedDate;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public Bill() {}
    
    public Bill(String basePrintNoStr, String title, String summary, String memo, 
                String chamber, Integer year, String sponsorName, String status, LocalDateTime publishedDate) {
        this.basePrintNoStr = basePrintNoStr;
        this.title = title;
        this.summary = summary;
        this.memo = memo;
        this.chamber = chamber;
        this.year = year;
        this.sponsorName = sponsorName;
        this.status = status;
        this.publishedDate = publishedDate;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
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
    
    public LocalDateTime getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDateTime publishedDate) { this.publishedDate = publishedDate; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
