package ru.strbnm.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.dto.CartItemInfoDto;
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
  public ResponseEntity<CartItemInfoDto> addToCart(@RequestBody CartItemDto cartItemDto) {
    CartItemDto savedItem =
        cartService.addToCart(cartItemDto.getProductId(), cartItemDto.getQuantity());
    CartItemInfoDto cartItemInfo = getCartItemInfoDto(savedItem);
    return ResponseEntity.ok(cartItemInfo);
  }

  @PutMapping
  @ResponseBody
  public ResponseEntity<CartItemInfoDto> updateCartItem(@RequestBody CartItemDto cartItemDto) {
    CartItemDto updatedItem =
        cartService.updateCartItem(
            cartItemDto.getId(), cartItemDto.getProductId(), cartItemDto.getQuantity());
    CartItemInfoDto cartItemInfo = getCartItemInfoDto(updatedItem);
    return ResponseEntity.ok(cartItemInfo);
  }

  @DeleteMapping("/{cartItemId}")
  @ResponseBody
  public ResponseEntity<CartInfoDto> removeCartItem(
      @PathVariable(value = "cartItemId") Long cartItemId) {
    cartService.removeFromCart(cartItemId);
    CartInfoDto cartInfo = cartService.getCartInfo();
    return ResponseEntity.ok(cartInfo);
  }

  @GetMapping
  public String getCart(Model m) {
    CartDto cart = cartService.getCartSummary();
    // Вычисляем сумму количества товаров в корзине
    int cartTotalQuantity = cart.getCartItems().stream().mapToInt(CartItem::getQuantity).sum();
    m.addAttribute("cart", cart);
    m.addAttribute("cartTotalQuantity", cartTotalQuantity);
    return "cart/index";
  }

  private CartItemInfoDto getCartItemInfoDto(CartItemDto cartItem) {
    CartInfoDto cartInfo = cartService.getCartInfo();
    return CartItemInfoDto.builder()
        .cartItem(cartItem)
        .cartItemsCount(cartInfo.getCartItemsCount())
        .cartAmount(cartInfo.getCartAmount())
        .build();
  }
}
