package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, Long> {

    Optional<EmailVerificationOtp> findByUserIdAndCode(Long userId, String code);

    @Modifying
    @Query("UPDATE EmailVerificationOtp e SET e.used = true WHERE e.userId = :userId AND e.used = false")
    void invalidateActiveOtps(@Param("userId") Long userId);
}
