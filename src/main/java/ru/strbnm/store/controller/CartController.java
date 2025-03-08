package ru.strbnm.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.dto.CartItemInfoDto;
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
  public Mono<ResponseEntity<CartItemInfoDto>> addToCart(@RequestBody CartItemDto cartItemDto) {
    return cartService
        .addToCart(cartItemDto.getProductId(), cartItemDto.getQuantity())
        .flatMap(this::getCartItemInfoDto)
        .map(ResponseEntity::ok);
  }

  @PutMapping
  @ResponseBody
  public Mono<ResponseEntity<CartItemInfoDto>> updateCartItem(
      @RequestBody CartItemDto cartItemDto) {
    return cartService
        .updateCartItem(cartItemDto.getId(), cartItemDto.getProductId(), cartItemDto.getQuantity())
        .flatMap(this::getCartItemInfoDto)
        .map(ResponseEntity::ok);
  }

  @DeleteMapping("/{cartItemId}")
  @ResponseBody
  public Mono<ResponseEntity<CartInfoDto>> removeCartItem(@PathVariable Long cartItemId) {
    return cartService
        .removeFromCart(cartItemId)
        .then(cartService.getCartInfo())
        .map(ResponseEntity::ok);
  }

  @GetMapping
  public Mono<String> getCart(Model m) {
    return cartService
        .getCartSummary()
        .doOnNext(cart -> m.addAttribute("cart", cart))
        .then(Mono.just("cart/index"));
  }

  private Mono<CartItemInfoDto> getCartItemInfoDto(CartItemDto cartItem) {
    return cartService
        .getCartInfo()
        .map(
            cartInfo ->
                CartItemInfoDto.builder()
                    .cartItem(cartItem)
                    .cartItemsCount(cartInfo.getCartItemsCount())
                    .cartAmount(cartInfo.getCartAmount())
                    .build());
  }
}
