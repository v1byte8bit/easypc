package com.example.easypc.data.repository;

import com.example.easypc.data.entity.Notification;
import com.example.easypc.data.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long id);
}