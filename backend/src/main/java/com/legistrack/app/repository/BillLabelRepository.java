package com.legistrack.app.repository;

import com.legistrack.app.model.BillLabel;
import com.legistrack.app.model.BillLabelId;
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
public interface BillLabelRepository extends JpaRepository<BillLabel, BillLabelId> {
    
    @Query("SELECT bl FROM BillLabel bl WHERE bl.bill.id = :billId")
    List<BillLabel> findByBill_Id(@Param("billId") UUID billId);
    
    @Query("SELECT bl FROM BillLabel bl WHERE bl.label.id = :labelId")
    List<BillLabel> findByLabel_Id(@Param("labelId") UUID labelId);
    
    @Query("SELECT bl FROM BillLabel bl WHERE bl.bill.id = :billId AND bl.label.id = :labelId")
    Optional<BillLabel> findByBill_IdAndLabel_Id(@Param("billId") UUID billId, @Param("labelId") UUID labelId);
    
    @Query("SELECT COUNT(bl) > 0 FROM BillLabel bl WHERE bl.bill.id = :billId AND bl.label.id = :labelId")
    boolean existsByBill_IdAndLabel_Id(@Param("billId") UUID billId, @Param("labelId") UUID labelId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM BillLabel bl WHERE bl.bill.id = :billId AND bl.label.id = :labelId")
    void deleteByBill_IdAndLabel_Id(@Param("billId") UUID billId, @Param("labelId") UUID labelId);
}

