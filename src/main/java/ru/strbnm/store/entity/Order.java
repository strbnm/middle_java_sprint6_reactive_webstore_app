package ru.strbnm.store.entity;

import java.math.BigDecimal;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@Builder
@Table("orders")
public class Order {
  @Id private Long id;

  @Column("total_price")
  private BigDecimal totalPrice;
}
