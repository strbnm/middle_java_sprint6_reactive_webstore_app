package ru.strbnm.store.service;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;

public interface CartService {
  List<CartItemDto> getCartItems();

  @Transactional
  CartItemDto addToCart(Long productId, int quantity);

  @Transactional
  CartItemDto updateCartItem(Long id, Long productId, int quantity);

  @Transactional
  void removeFromCart(Long productId);

  @Transactional
  CartDto getCartSummary();

  @Transactional
  CartInfoDto getCartInfo();
}
