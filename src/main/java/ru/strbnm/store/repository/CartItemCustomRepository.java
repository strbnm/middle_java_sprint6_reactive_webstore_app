package ru.strbnm.store.repository;

import reactor.core.publisher.Flux;
import ru.strbnm.store.dto.CartItemWithProduct;

public interface CartItemCustomRepository {
  Flux<CartItemWithProduct> findAllWithProducts();
}
