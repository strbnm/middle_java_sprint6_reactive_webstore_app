package ru.strbnm.store.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
  private Long id;
  private BigDecimal totalPrice;
  private List<OrderItemDto> items;
}
