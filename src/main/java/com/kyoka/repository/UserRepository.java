package com.kyoka.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUserName(String username);
    boolean existsByUserName(String username);
    Boolean existsByEmail(String email);
}
