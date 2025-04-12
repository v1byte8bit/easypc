package com.example.easypc.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "source")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "source_id_gen")
    @SequenceGenerator(name = "source_id_gen", sequenceName = "source_id_source_seq", allocationSize = 1)
    @Column(name = "id_source", nullable = false)
    private Integer id;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "category", nullable = false)
    private String category;
}