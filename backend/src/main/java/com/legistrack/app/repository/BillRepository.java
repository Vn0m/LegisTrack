package com.legistrack.app.repository;

import com.legistrack.app.model.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    
    Optional<Bill> findByBasePrintNoStr(String basePrintNoStr);
    
    @Query("SELECT b FROM Bill b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Bill> findByTitleContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT b FROM Bill b WHERE LOWER(b.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Bill> findBySummaryContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT b FROM Bill b WHERE LOWER(b.sponsorName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Bill> findBySponsorNameContainingIgnoreCase(@Param("searchTerm") String searchTerm);
    
    List<Bill> findByYear(Integer year);
    
    List<Bill> findByChamber(String chamber);
    
    List<Bill> findByStatus(String status);
    
    @Query("SELECT b FROM Bill b WHERE " +
           "(:searchTerm IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.summary) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.sponsorName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "(:year IS NULL OR b.year = :year) AND " +
           "(:chamber IS NULL OR b.chamber = :chamber)")
    List<Bill> searchBills(@Param("searchTerm") String searchTerm, 
                          @Param("year") Integer year, 
                          @Param("chamber") String chamber);
}
