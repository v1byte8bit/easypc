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
@Table(name = "build")
public class Build {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Long userId;
    @Column(nullable = true, precision = 10)
    private Double totalPrice;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "build")
    private List<BuildItem> items;
}

