package ru.strbnm.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderSummaryDto;
import ru.strbnm.store.service.CartService;
import ru.strbnm.store.service.OrderService;

import java.net.URI;

@Controller
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;
  private final CartService cartService;

  @Autowired
  public OrderController(OrderService orderService, CartService cartService) {
    this.orderService = orderService;
    this.cartService = cartService;
  }

  @Transactional
  @PostMapping("/checkout")
  public Mono<ResponseEntity<Void>> checkoutCartAndCreateOrder() {
    return orderService
        .createOrder()
        .map(order -> URI.create("/orders/" + order.getId()))
        .map(uri -> ResponseEntity.status(HttpStatus.FOUND).location(uri).build());
  }

  //  @Transactional
  //  @PostMapping("/checkout")
  //  public Mono<String> checkoutCartAndCreateOrder() {
  //    return orderService.createOrder()
  //            .map(order -> "redirect:/orders/" + order.getId());
  //  }

  @GetMapping
  public Mono<String> getOrders(Model m) {
    return orderService
        .getOrdersSummary()
        .zipWith(cartService.getCartInfo())
        .doOnNext(
            tuple -> {
              OrderSummaryDto ordersSummary = tuple.getT1();
              CartInfoDto cartInfo = tuple.getT2();

              m.addAttribute("orders", ordersSummary.getOrderDtoList());
              m.addAttribute("totalOrdersAmount", ordersSummary.getTotalAmount());
              m.addAttribute("cartTotalQuantity", cartInfo.getCartItemsCount());
            })
        .thenReturn("orders/index");
  }

  @GetMapping("/{orderId}")
  public Mono<String> getOrderById(@PathVariable Long orderId, Model m) {
    return orderService
        .getOrderById(orderId)
        .zipWith(cartService.getCartInfo())
        .doOnNext(
            tuple -> {
              OrderDto order = tuple.getT1();
              CartInfoDto cartInfo = tuple.getT2();

              m.addAttribute("order", order);
              m.addAttribute("cartTotalQuantity", cartInfo.getCartItemsCount());
              m.addAttribute("title", "Состав заказа #" + order.getId());
            })
        .thenReturn("orders/order_detail");
  }
}
