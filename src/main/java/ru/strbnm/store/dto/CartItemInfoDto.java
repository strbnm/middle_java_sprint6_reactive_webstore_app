package ru.strbnm.store.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CartItemInfoDto {
  CartItemDto cartItem;
  Integer cartItemsCount;
  BigDecimal cartAmount;
}
