package ru.strbnm.store.dto;

import java.math.BigDecimal;
import lombok.Value;

@Value
public class CartInfoDto {
  Integer cartItemsCount;
    BigDecimal cartAmount;
}
