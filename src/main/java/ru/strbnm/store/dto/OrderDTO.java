package ru.strbnm.store.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
  private Long id;
  private BigDecimal totalPrice;
  private List<OrderItemDTO> items;
}
