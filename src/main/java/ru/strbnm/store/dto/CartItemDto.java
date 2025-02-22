package ru.strbnm.store.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemDto {
  private Long id;
  private Long productId;
  private int quantity;
}
