package com.legistrack.app.repository;

import com.legistrack.app.model.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, UUID> {
    
    @Query("SELECT a FROM AiSummary a WHERE a.bill.id = :billId")
    Optional<AiSummary> findByBillId(@Param("billId") UUID billId);
    
    boolean existsByBill_Id(UUID billId);
    
    @Query("SELECT ai FROM AiSummary ai JOIN FETCH ai.bill WHERE ai.bill.id = :billId")
    Optional<AiSummary> findByBillIdWithBillDetails(@Param("billId") UUID billId);
}
