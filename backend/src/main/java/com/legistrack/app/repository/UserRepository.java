package com.legistrack.app.repository;

import com.legistrack.app.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findById(UUID id);
    
    User save(User user);
    
    void deleteById(UUID id);
 
    boolean existsByEmail(String email);
}
