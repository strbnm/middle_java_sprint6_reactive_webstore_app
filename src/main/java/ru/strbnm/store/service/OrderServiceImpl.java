package ru.strbnm.store.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.strbnm.store.dto.OrderDTO;
import ru.strbnm.store.dto.OrderSummaryDTO;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Order;
import ru.strbnm.store.entity.OrderItem;
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

  @Autowired
  public OrderServiceImpl(
      CartItemRepository cartItemRepository,
      OrderRepository orderRepository,
      OrderItemRepository orderItemRepository,
      OrderMapper orderMapper) {
    this.cartItemRepository = cartItemRepository;
    this.orderRepository = orderRepository;
    this.orderItemRepository = orderItemRepository;
    this.orderMapper = orderMapper;
  }

  @Override
  @Transactional
  public OrderDTO createOrder() {
    List<CartItem> cartItems = cartItemRepository.findAll();
    if (cartItems.isEmpty()) {
      throw new RuntimeException("Cart is empty");
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
  public List<OrderDTO> getAllOrders() {
    return orderMapper.toDTOs(orderRepository.findAll());
  }

  @Override
  public OrderDTO getOrderById(Long orderId) {
    return orderRepository
        .findById(orderId)
        .map(orderMapper::toDTO)
        .orElseThrow(() -> new RuntimeException("Заказ не найден."));
  }

  @Override
  public OrderSummaryDTO getOrdersSummary() {
    List<OrderDTO> orderDTOList = getAllOrders();
    BigDecimal totalAmount =
        orderDTOList.stream().map(OrderDTO::getTotalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
    return new OrderSummaryDTO(orderDTOList, totalAmount);
  }
}
