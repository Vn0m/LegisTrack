package com.legistrack.app.repository;

import com.legistrack.app.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {

    Optional<Bill> findByBasePrintNoStr(String basePrintNoStr);

    List<Bill> findByBasePrintNoStrIn(Collection<String> basePrintNoStrs);

    @Query("SELECT b FROM Bill b WHERE " +
           "(CAST(:searchTerm AS string) IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR " +
           "LOWER(b.summary) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%')) OR " +
           "LOWER(b.sponsorName) LIKE LOWER(CONCAT('%', CAST(:searchTerm AS string), '%'))) AND " +
           "(:year IS NULL OR b.year = :year) AND " +
           "(CAST(:chamber AS string) IS NULL OR LOWER(b.chamber) = LOWER(CAST(:chamber AS string))) AND " +
           "(CAST(:status AS string) IS NULL OR LOWER(b.status) LIKE LOWER(CONCAT('%', CAST(:status AS string), '%'))) AND " +
           "(CAST(:committee AS string) IS NULL OR LOWER(b.committeeName) LIKE LOWER(CONCAT('%', CAST(:committee AS string), '%')))")
    List<Bill> searchBills(@Param("searchTerm") String searchTerm,
                           @Param("year") Integer year,
                           @Param("chamber") String chamber,
                           @Param("status") String status,
                           @Param("committee") String committee);

    @Query(value = """
        SELECT base_print_no_str AS "basePrintNoStr", title, summary, chamber, year,
               sponsor_name AS "sponsorName", status, committee_name AS "committeeName",
               1 - (content_embedding <=> CAST(:embedding AS vector)) AS "score"
        FROM bills
        WHERE content_embedding IS NOT NULL
        ORDER BY content_embedding <=> CAST(:embedding AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<BillSearchHit> findSimilarBills(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = "SELECT * FROM bills WHERE content_embedding IS NULL LIMIT 100", nativeQuery = true)
    List<Bill> findBillsWithoutEmbeddings();

    @Query(value = "SELECT content_embedding IS NOT NULL FROM bills WHERE id = :id", nativeQuery = true)
    Boolean hasEmbedding(@Param("id") UUID id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE bills SET content_embedding = CAST(:embedding AS vector) WHERE id = :id", nativeQuery = true)
    void updateEmbedding(@Param("id") UUID id, @Param("embedding") String embedding);
}
