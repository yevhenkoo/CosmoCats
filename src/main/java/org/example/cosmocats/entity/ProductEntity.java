package org.example.cosmocats.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString(exclude = "category")
@Table(
    name = "products",
    indexes = {@Index(name = "idx_product_name", columnList = "name")},
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_product_name_category",
          columnNames = {"name", "category_id"})
    })
public class ProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
  @SequenceGenerator(name = "product_seq", sequenceName = "product_seq", allocationSize = 50)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  private double price;

  private String sku;

  private int stockQuantity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  private CategoryEntity category;
}
