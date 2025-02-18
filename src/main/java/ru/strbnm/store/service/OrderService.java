package ru.strbnm.store.service;

import java.util.List;
import ru.strbnm.store.dto.OrderDTO;
import ru.strbnm.store.dto.OrderSummaryDTO;

public interface OrderService {
  OrderDTO createOrder();

  List<OrderDTO> getAllOrders();

  OrderDTO getOrderById(Long orderId);

  OrderSummaryDTO getOrdersSummary();
}
