package ru.strbnm.store.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.CartItemWithProduct;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderSummaryDto;
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
    public Mono<OrderDto> createOrder() {
        return cartItemRepository.findAllWithProducts()
                .collectList()
                .filter(cartItems -> !cartItems.isEmpty())
                .switchIfEmpty(Mono.error(new RuntimeException("Корзина пустая.")))
                .flatMap(this::processCartItems)
                .map(orderMapper::toDTO);
    }

    private Mono<Order> processCartItems(List<CartItemWithProduct> cartItems) {
        BigDecimal totalPrice = calculateTotalPrice(cartItems);
        Order newOrder = Order.builder().totalPrice(totalPrice).build();

        return orderRepository.save(newOrder)
                .flatMap(savedOrder -> saveOrderItemsAndClearCart(cartItems, savedOrder));
    }

    private BigDecimal calculateTotalPrice(List<CartItemWithProduct> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getCartItem().getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<Order> saveOrderItemsAndClearCart(List<CartItemWithProduct> cartItems, Order savedOrder) {
        List<OrderItem> orderItems = mapCartItemsToOrderItems(cartItems, savedOrder.getId());

        return orderItemRepository.saveAll(orderItems)
                .then(cartItemRepository.deleteAll())
                .thenReturn(savedOrder);
    }

    private List<OrderItem> mapCartItemsToOrderItems(List<CartItemWithProduct> cartItems, Long orderId) {
        return cartItems.stream()
                .map(cartItemWithProduct ->
                        OrderItem.builder()
                                .productId(cartItemWithProduct.getProduct().getId())
                                .quantity(cartItemWithProduct.getCartItem().getQuantity())
                                .price(cartItemWithProduct.getProduct().getPrice())
                                .orderId(orderId)
                                .build())
                .toList();
    }

    @Override
    public Flux<OrderDto> getAllOrders() {
        return orderRepository.findAllOrdersWithItemsAndProducts();
    }

    @Override
    public Mono<OrderDto> getOrderById(Long orderId) {
        return orderRepository.findOrderWithItemsAndProducts(orderId);
    }

    @Override
    public Mono<OrderSummaryDto> getOrdersSummary() {
        return orderRepository.findAllOrdersWithItemsAndProducts()
                .reduce(new OrderSummaryDto(new ArrayList<>(), BigDecimal.ZERO), this::accumulateOrderSummary);
    }

    private OrderSummaryDto accumulateOrderSummary(OrderSummaryDto summary, OrderDto order) {
        summary.getOrderDtoList().add(order);
        return new OrderSummaryDto(summary.getOrderDtoList(), summary.getTotalAmount().add(order.getTotalPrice()));
    }
}
