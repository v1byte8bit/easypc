package com.example.easypc.data.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "build_item")
public class BuildItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "build_item_id_gen")
    @SequenceGenerator(name = "build_item_id_gen", sequenceName = "build_item_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "build_id", nullable = false)
    private Build build;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_url", referencedColumnName = "id_source", nullable = false)
    private Source productUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}