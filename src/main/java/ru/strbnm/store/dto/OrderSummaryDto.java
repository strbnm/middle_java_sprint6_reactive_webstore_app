package ru.strbnm.store.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Value;

@Value
public class OrderSummaryDto {
  List<OrderDto> orderDtoList;
  BigDecimal totalAmount;
}
