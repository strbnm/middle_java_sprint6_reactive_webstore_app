package ru.strbnm.store.service;

import org.springframework.transaction.annotation.Transactional;
import ru.strbnm.store.dto.CartDTO;
import ru.strbnm.store.dto.CartItemDTO;

import java.util.List;

public interface CartService {
    List<CartItemDTO> getCartItems();
    void addToCart(Long productId, int quantity);
    void updateCartItemQuantity(Long productId, int quantity);
    void removeFromCart(Long productId);
    CartDTO getCartSummary();
}
