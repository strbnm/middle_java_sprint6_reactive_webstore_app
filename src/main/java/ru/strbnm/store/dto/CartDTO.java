package ru.strbnm.store.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Value;
import ru.strbnm.store.entity.CartItem;

@Value
public class CartDto {
  List<CartItem> cartItemsCount;
  BigDecimal cartAmount;
}
