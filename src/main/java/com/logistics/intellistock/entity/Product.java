package com.logistics.intellistock.entity;

import com.logistics.intellistock.entity.enums.Category;
import com.logistics.intellistock.entity.enums.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private Category category;

  @Column(nullable = false, unique = true, length = 50)
  private String sku;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal sellingPrice;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String purchasePriceEncrypted;

  @Column(columnDefinition = "TEXT")
  private String marginEncrypted;

  @Column(precision = 10, scale = 2)
  private BigDecimal weight;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Unit unit;

  @Column(nullable = false)
  private Boolean isActive = true;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;
}
