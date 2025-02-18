package ru.strbnm.store.service;

import java.util.List;
import ru.strbnm.store.dto.CartDTO;
import ru.strbnm.store.dto.CartItemDTO;

public interface CartService {
  List<CartItemDTO> getCartItems();

  void addToCart(Long productId, int quantity);

  void updateCartItemQuantity(Long productId, int quantity);

  void removeFromCart(Long productId);

  CartDTO getCartSummary();
}
