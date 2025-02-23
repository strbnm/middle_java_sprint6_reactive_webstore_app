package ru.strbnm.store.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {
  private final CartService cartService;

  @Autowired
  public CartController(CartService cartService) {
    this.cartService = cartService;
  }

  @PostMapping
  @ResponseBody
  public ResponseEntity<CartItemDto> addToCart(@RequestBody CartItemDto cartItemDto) {
    CartItemDto savedItem =
        cartService.addToCart(cartItemDto.getProductId(), cartItemDto.getQuantity());
    return ResponseEntity.ok(savedItem);
  }

  @PutMapping
  @ResponseBody
  public ResponseEntity<CartItemDto> updateCartItem(@RequestBody CartItemDto cartItemDto) {
    CartItemDto updatedItem =
        cartService.updateCartItem(
            cartItemDto.getId(), cartItemDto.getProductId(), cartItemDto.getQuantity());
    return ResponseEntity.ok(updatedItem);
  }

  @DeleteMapping("/{cartItemId}")
  @ResponseBody
  public String removeCartItem(@PathVariable(value = "cartItemId") Long cartItemId) {
    cartService.removeFromCart(cartItemId);
    return "success";
  }

  @GetMapping
  public String getCart(Model m) {
    CartDto cart = cartService.getCartSummary();
    // Вычисляем сумму количества товаров в корзине
    int cartTotalQuantity = cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum();
    m.addAttribute("cart", cart);
    m.addAttribute("totalQuantity", cartTotalQuantity);
    return "cart/showcart";
  }
}
