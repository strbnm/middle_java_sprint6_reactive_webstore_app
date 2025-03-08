package ru.strbnm.store.service;

import java.util.Map;

import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;

public interface CartService {
  Flux<CartItemDto> getCartItemsDto();

  @Transactional
  Mono<CartItemDto> addToCart(Long productId, int quantity);

  @Transactional
  Mono<CartItemDto> updateCartItem(Long id, Long productId, int quantity);

  @Transactional
  Mono<Void> removeFromCart(Long cartItemId);

  @Transactional
  Mono<CartDto> getCartSummary();

  @Transactional
  Mono<CartInfoDto> getCartInfo();

  Mono<Map<Long, CartItemDto>> getCartItemMap();
}
