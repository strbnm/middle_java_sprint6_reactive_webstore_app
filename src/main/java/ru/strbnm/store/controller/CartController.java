package ru.strbnm.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.service.CartService;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
  private final CartService cartService;

  @PostMapping
  public ResponseEntity<CartItemDto> addToCart(@RequestBody CartItemDto cartItemDto) {
    CartItemDto savedItem =
        cartService.addToCart(cartItemDto.getProductId(), cartItemDto.getQuantity());
    return ResponseEntity.ok(savedItem);
  }

  @PutMapping
  public ResponseEntity<CartItemDto> updateCartItem(@RequestBody CartItemDto cartItemDto) {
    CartItemDto updatedItem =
        cartService.updateCartItem(
            cartItemDto.getId(), cartItemDto.getProductId(), cartItemDto.getQuantity());
    return ResponseEntity.ok(updatedItem);
  }
}
