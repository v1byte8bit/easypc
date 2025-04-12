package com.example.easypc.data.entity;

import lombok.Data;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
@Entity
@Getter
@Setter
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idOrder;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "builder_id", nullable = true)
    private User builder;

    @Column(nullable = true, length = 50)
    private String status;

    @Column(nullable = true, precision = 10)
    private Double totalPrice;

    @Column
    private String address;

    @Column(nullable = true, precision = 10)
    private String phone;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "order")
    private List<OrderItem> items;
}