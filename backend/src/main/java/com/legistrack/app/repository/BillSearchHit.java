package com.legistrack.app.repository;

/**
 * Projection for pgvector similarity queries: bill columns plus the cosine
 * similarity score, which has no home on the entity.
 */
public interface BillSearchHit {
    String getBasePrintNoStr();
    String getTitle();
    String getSummary();
    String getChamber();
    Integer getYear();
    String getSponsorName();
    String getStatus();
    String getCommitteeName();
    Double getScore();
}
