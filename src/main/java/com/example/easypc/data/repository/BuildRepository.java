package com.example.easypc.data.repository;

import com.example.easypc.data.entity.Build;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BuildRepository extends JpaRepository<Build, Long> {
    Build findByUserIdAndName(Long userId, String buildName);
    Optional<Build> findByIdAndUserId(Long buildId, Long userId);
    List<Build> findByUserId(Long userId);
}
