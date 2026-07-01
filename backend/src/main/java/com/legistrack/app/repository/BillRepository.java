package com.legistrack.app.repository;

import com.legistrack.app.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {

    Optional<Bill> findByBasePrintNoStr(String basePrintNoStr);

    List<Bill> findByYear(Integer year);
    List<Bill> findByChamber(String chamber);
    List<Bill> findByStatus(String status);

    @Query("SELECT b FROM Bill b WHERE " +
           "(:searchTerm IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.sponsorName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:year IS NULL OR b.year = :year) AND " +
           "(:chamber IS NULL OR LOWER(b.chamber) = LOWER(:chamber)) AND " +
           "(:status IS NULL OR LOWER(b.status) LIKE LOWER(CONCAT('%', :status, '%'))) AND " +
           "(:committee IS NULL OR LOWER(b.committeeName) LIKE LOWER(CONCAT('%', :committee, '%')))")
    List<Bill> searchBills(@Param("searchTerm") String searchTerm,
                           @Param("year") Integer year,
                           @Param("chamber") String chamber,
                           @Param("status") String status,
                           @Param("committee") String committee);

    @Query(value = "SELECT * FROM bills WHERE content_embedding IS NOT NULL ORDER BY content_embedding <=> CAST(:embedding AS vector) LIMIT :limit", nativeQuery = true)
    List<Bill> findSimilarBills(@Param("embedding") String embedding, @Param("limit") int limit);

    @Query(value = "SELECT * FROM bills WHERE content_embedding IS NULL LIMIT 100", nativeQuery = true)
    List<Bill> findBillsWithoutEmbeddings();

    @Modifying
    @Transactional
    @Query(value = "UPDATE bills SET content_embedding = CAST(:embedding AS vector) WHERE id = CAST(:id AS uuid)", nativeQuery = true)
    void updateEmbedding(@Param("id") String id, @Param("embedding") String embedding);
}
