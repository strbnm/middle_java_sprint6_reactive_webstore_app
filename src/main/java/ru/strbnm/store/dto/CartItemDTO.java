package ru.strbnm.store.dto;


import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class CartItemDTO {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}
