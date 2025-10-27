package com.legistrack.app.repository;

import com.legistrack.app.model.SavedBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedBillRepository extends JpaRepository<SavedBill, UUID> {
    
    List<SavedBill> findByUserId(UUID userId);
    
    Optional<SavedBill> findByUserIdAndBillId(UUID userId, UUID billId);
    
    boolean existsByUserIdAndBillId(UUID userId, UUID billId);
    
    void deleteByUserIdAndBillId(UUID userId, UUID billId);
    
    @Query("SELECT sb FROM SavedBill sb JOIN FETCH sb.bill WHERE sb.userId = :userId ORDER BY sb.savedAt DESC")
    List<SavedBill> findByUserIdWithBillDetails(@Param("userId") UUID userId);
}
