package ru.strbnm.store.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderSummaryDto;

public interface OrderService {
  Mono<OrderDto> createOrder();

  Flux<OrderDto> getAllOrders();

  Mono<OrderDto> getOrderById(Long orderId);

  Mono<OrderSummaryDto> getOrdersSummary();
}
