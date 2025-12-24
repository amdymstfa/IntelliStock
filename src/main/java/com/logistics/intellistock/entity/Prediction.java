package com.logistics.intellistock.entity;

import jakarta.persistence.*;
  import lombok.*;
  import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "predictions",
  indexes = {
    @Index(name = "idx_prediction_date", columnList = "predictionDate"),
    @Index(name = "idx_product_warehouse_pred", columnList = "product_id, warehouse_id")
  })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prediction {

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
  private LocalDate predictionDate;

  @Column(nullable = false)
  private Integer predictedQuantity30Days;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal confidenceLevel;

  @Column(columnDefinition = "TEXT")
  private String recommendation;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdAt;
}
