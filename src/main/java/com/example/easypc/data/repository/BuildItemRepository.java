package com.example.easypc.data.repository;

import com.example.easypc.data.entity.Build;
import com.example.easypc.data.entity.BuildItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BuildItemRepository extends JpaRepository<BuildItem, Long> {
    void deleteByBuild(Build build);

    List<BuildItem> findByBuild(Build build);
}
