package ru.strbnm.store.dto;

import java.math.BigDecimal;
import lombok.*;

@Value
public class OrderItemDto {
  Long productId;
  String productName;
  String productImageUrl;
  int quantity;
  BigDecimal price;
}
