package ru.strbnm.store.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import lombok.*;

@Value
public class CartItemDTO {
  Long productId;
  String productName;
  String productImageUrl;

  @Positive(message = "Количество товара должно быть положительным числом.")
  int quantity;

  @Positive(message = "Цена товара должна быть положительным числом.")
  BigDecimal productPrice;
}
