package ru.strbnm.store.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderSummaryDto;
import ru.strbnm.store.service.CartService;
import ru.strbnm.store.service.OrderService;

@Controller
@RequestMapping("/orders")
public class OrderController {
  private final OrderService orderService;
  private final CartService cartService;

  @Autowired
  public OrderController(
      OrderService orderService, CartService cartService, CartService cartService1) {
    this.orderService = orderService;
    this.cartService = cartService1;
  }

  @Transactional
  @PostMapping("/checkout")
  public String checkoutCartAndCreateOrder() {
    OrderDto createdOrder = orderService.createOrder();
    return "redirect:/orders/" + createdOrder.getId();
  }

  @GetMapping
  public String getOrders(Model m) {
    OrderSummaryDto ordersSummary = orderService.getOrdersSummary();
    Integer cartTotalQuantity = cartService.getCartInfo().getCartItemsCount();
    m.addAttribute("orders", ordersSummary.getOrderDtoList());
    m.addAttribute("cartTotalQuantity", cartTotalQuantity);
    m.addAttribute("totalOrdersAmount", ordersSummary.getTotalAmount());
    return "orders/index";
  }

  @GetMapping("/{orderId}")
  public String getOrderById(@PathVariable Long orderId, Model m) {
    OrderDto order = orderService.getOrderById(orderId);
    Integer cartTotalQuantity = cartService.getCartInfo().getCartItemsCount();
    m.addAttribute("order", order);
    m.addAttribute("cartTotalQuantity", cartTotalQuantity);
    m.addAttribute("title", "Состав заказа #" + order.getId());
    return "orders/order_detail";
  }
}
