package ru.strbnm.store.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.OrderDto;

public interface OrderCustomRepository {
  Mono<OrderDto> findOrderWithItemsAndProducts(Long orderId);

  Flux<OrderDto> findAllOrdersWithItemsAndProducts();
}
