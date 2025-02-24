package ru.strbnm.store.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderItemDto;
import ru.strbnm.store.dto.OrderSummaryDto;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Order;
import ru.strbnm.store.entity.OrderItem;
import ru.strbnm.store.mapper.OrderItemMapper;
import ru.strbnm.store.mapper.OrderMapper;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.OrderItemRepository;
import ru.strbnm.store.repository.OrderRepository;

@Service
public class OrderServiceImpl implements OrderService {
  private final CartItemRepository cartItemRepository;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;
  private final OrderMapper orderMapper;
  private final OrderItemMapper orderItemMapper;

  @Autowired
  public OrderServiceImpl(
      CartItemRepository cartItemRepository,
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      OrderMapper orderMapper,
      OrderItemMapper orderItemMapper) {
    this.cartItemRepository = cartItemRepository;
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.orderMapper = orderMapper;
    this.orderItemMapper = orderItemMapper;
  }

  @Transactional
  @Override
  public OrderDto createOrder() {
    List<CartItem> cartItems = cartItemRepository.findAll();
    if (cartItems.isEmpty()) {
      throw new RuntimeException("Корзина пустая.");
    }

    BigDecimal totalPrice =
        cartItems.stream()
            .map(
                item ->
                    item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    Order order = Order.builder().totalPrice(totalPrice).build();

    order = orderRepository.save(order);

    Order finalOrder = order;
    List<OrderItem> orderItems =
        cartItems.stream()
            .map(
                cartItem ->
                    OrderItem.builder()
                        .product(cartItem.getProduct())
                        .quantity(cartItem.getQuantity())
                        .price(cartItem.getProduct().getPrice())
                        .order(finalOrder)
                        .build())
            .toList();

    orderItemRepository.saveAll(orderItems);
    cartItemRepository.deleteAll();

    return orderMapper.toDTO(order);
  }

  @Override
  public List<OrderDto> getAllOrders() {
    return orderMapper.toDTOs(orderRepository.findAll());
  }

  @Override
  public OrderDto getOrderById(Long orderId) {
    Order order =
        orderRepository
            .findOrderWithItemsAndProducts(orderId)
            .orElseThrow(() -> new RuntimeException("Заказ не найден."));
    List<OrderItemDto> items = orderItemMapper.toDTOs(order.getItems());
    return OrderDto.builder()
        .id(order.getId())
        .totalPrice(order.getTotalPrice())
        .items(items)
        .build();
  }

  @Override
  public OrderSummaryDto getOrdersSummary() {
    List<OrderDto> orderDtoList = getAllOrders();
    BigDecimal totalAmount =
        orderDtoList.stream().map(OrderDto::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new OrderSummaryDto(orderDtoList, totalAmount);
  }
}
