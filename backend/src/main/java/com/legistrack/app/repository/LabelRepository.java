package com.legistrack.app.repository;

import com.legistrack.app.model.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabelRepository extends JpaRepository<Label, UUID> {
    
    Optional<Label> findByLabel(String label);
    
    boolean existsByLabel(String label);
}

