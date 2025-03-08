package ru.strbnm.store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.strbnm.store.entity.CartItem;

@Repository
public interface CartItemRepository
    extends ReactiveCrudRepository<CartItem, Long>, CartItemCustomRepository {
  Mono<CartItem> findByProductId(Long productId);
}
