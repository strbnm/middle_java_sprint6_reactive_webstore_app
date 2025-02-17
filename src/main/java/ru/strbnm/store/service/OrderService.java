package ru.strbnm.store.service;

import ru.strbnm.store.dto.OrderDTO;
import ru.strbnm.store.dto.OrderSummaryDTO;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder();
    List<OrderDTO> getAllOrders();
    OrderDTO getOrderById(Long orderId);
    OrderSummaryDTO getOrdersSummary();
}
