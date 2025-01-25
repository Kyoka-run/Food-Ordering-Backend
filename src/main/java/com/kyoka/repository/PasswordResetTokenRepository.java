package com.kyoka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kyoka.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    PasswordResetToken findByToken(String token);
}
