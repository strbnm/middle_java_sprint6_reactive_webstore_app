package ru.strbnm.store.entity;

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
@Table("cart_items")
public class CartItem {
  @Id
  private Long id;

  @Column("product_id")
  private Product productId;

  private int quantity;
}
