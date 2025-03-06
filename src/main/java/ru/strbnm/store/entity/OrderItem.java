package ru.strbnm.store.entity;

import java.math.BigDecimal;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table("order_items")
public class OrderItem {
  @Id
  private Long id;

  @Column("product_id")
  private Long productId;

  private int quantity;

  private BigDecimal price;

  @Column("order_id")
  private Long orderId;
}

