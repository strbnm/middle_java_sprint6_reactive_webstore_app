package ru.strbnm.store.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
class OrderDTO {
    private Long id;
    private BigDecimal totalPrice;
    private List<OrderItemDTO> items;
}