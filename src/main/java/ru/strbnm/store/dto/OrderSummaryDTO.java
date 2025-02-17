package ru.strbnm.store.dto;

import lombok.Value;

import java.math.BigDecimal;
import java.util.List;

@Value
public class OrderSummaryDTO {
    List<OrderDTO> orderDTOList;
    BigDecimal totalAmount;
}
