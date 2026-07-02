package com.legistrack.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.legistrack.app.model.Bill;
import com.legistrack.app.repository.BillSearchHit;

/**
 * The one bill shape every list endpoint returns. {@code score} is only present
 * on semantic search results (cosine similarity, 0..1).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BillSummaryDto(
    String basePrintNoStr,
    String title,
    String summary,
    String chamber,
    Integer year,
    String sponsorName,
    String status,
    String committeeName,
    Double score
) {
    public static BillSummaryDto from(Bill bill) {
        return new BillSummaryDto(bill.getBasePrintNoStr(), bill.getTitle(), bill.getSummary(),
            bill.getChamber(), bill.getYear(), bill.getSponsorName(), bill.getStatus(),
            bill.getCommitteeName(), null);
    }

    public static BillSummaryDto from(BillSearchHit hit) {
        return new BillSummaryDto(hit.getBasePrintNoStr(), hit.getTitle(), hit.getSummary(),
            hit.getChamber(), hit.getYear(), hit.getSponsorName(), hit.getStatus(),
            hit.getCommitteeName(), round(hit.getScore()));
    }

    private static Double round(Double score) {
        return score == null ? null : Math.round(score * 1000.0) / 1000.0;
    }
}
