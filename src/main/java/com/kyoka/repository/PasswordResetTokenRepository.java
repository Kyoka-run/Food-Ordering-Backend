package com.kyoka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.PasswordResetToken;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
}
