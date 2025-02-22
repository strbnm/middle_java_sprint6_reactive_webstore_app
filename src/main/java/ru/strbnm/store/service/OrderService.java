package ru.strbnm.store.service;

import java.util.List;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderSummaryDto;

public interface OrderService {
  OrderDto createOrder();

  List<OrderDto> getAllOrders();

  OrderDto getOrderById(Long orderId);

  OrderSummaryDto getOrdersSummary();
}
