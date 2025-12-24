package com.logistics.intellistock.entity;

import com.logistics.intellistock.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_history",
  indexes = {
    @Index(name = "idx_sale_date", columnList = "saleDate"),
    @Index(name = "idx_product_warehouse", columnList = "product_id, warehouse_id")
  })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "warehouse_id", nullable = false)
  private Warehouse warehouse;

  @Column(nullable = false)
  private LocalDate saleDate;

  @Column(nullable = false)
  private Integer quantitySold;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private DayOfWeek dayOfWeek;

  @Column(nullable = false)
  private Integer month;

  @Column(nullable = false)
  private Integer year;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;
}
