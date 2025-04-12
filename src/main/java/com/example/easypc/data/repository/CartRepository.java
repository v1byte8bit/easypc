package com.example.easypc.data.repository;

import com.example.easypc.data.entity.Cart;
import com.example.easypc.data.entity.Source;
import com.example.easypc.data.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUserAndSource(User user, Source source);
    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserIdAndSourceId(Long userId, Long sourceId);
    void deleteByUserIdAndSourceId(Long userId, Long sourceId);

    void deleteByUserId(Long userId);
}

