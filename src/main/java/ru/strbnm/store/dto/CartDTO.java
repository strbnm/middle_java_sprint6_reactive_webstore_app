package ru.strbnm.store.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class CartDTO {
    List<CartItemDTO> items;
    BigDecimal totalPrice;
}
