package com.medilinktunisia.authservice.repository;

import com.medilinktunisia.authservice.model.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
}
