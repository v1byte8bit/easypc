package com.example.easypc.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "notification_replacement_products")
public class NotificationReplacementProduct {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notification_id", nullable = false)
    private Notification notification;

    @Column(name = "product_url_id", nullable = false, length = Integer.MAX_VALUE)
    private String productUrlId;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}