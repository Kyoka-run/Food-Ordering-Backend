package com.kyoka.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kyoka.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u Where u.status='PENDING'")
    public List<User> getPendingRestaurantOwners();
    public User findByEmail(String email);
    Optional<User> findByUserName(String username);
    boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
}
