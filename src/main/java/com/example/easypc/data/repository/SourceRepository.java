package com.example.easypc.data.repository;

import com.example.easypc.data.entity.Source;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SourceRepository extends JpaRepository<Source, Long> {
    List<Source> findByCategory(String category);

}
