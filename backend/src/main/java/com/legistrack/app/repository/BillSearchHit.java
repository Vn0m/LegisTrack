package com.legistrack.app.repository;

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
