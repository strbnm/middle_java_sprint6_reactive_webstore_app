package ru.strbnm.store.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.strbnm.store.dto.CartItemWithProduct;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderItemDto;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Order;
import ru.strbnm.store.entity.OrderItem;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.OrderMapper;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.OrderItemRepository;
import ru.strbnm.store.repository.OrderRepository;

@SpringBootTest(classes = {OrderServiceImpl.class, OrderServiceImplTest.MapperTestConfig.class})
public class OrderServiceImplTest {

  @MockitoBean private OrderRepository orderRepository;

  @MockitoBean private OrderItemRepository orderItemRepository;

  @MockitoBean private CartItemRepository cartItemRepository;

  @Autowired private OrderMapper orderMapper;

  @Autowired private OrderServiceImpl orderService;

  private Product productA;
  private Product productB;
  private CartItem cartItemA;
  private CartItem cartItemB;
  private Order order;
  private OrderItem orderItemA;
  private OrderItem orderItemB;
  private OrderItemDto orderItemDtoA;
  private OrderItemDto orderItemDtoB;
  private CartItemWithProduct cartItemWithProductA;
  private CartItemWithProduct cartItemWithProductB;
  private OrderDto orderDto;

  @BeforeEach
  void setUp() {
    productA =
        Product.builder()
            .id(1L)
            .name("Test product A")
            .description("Test product A description")
            .imageUrl("test_product_a.png")
            .price(BigDecimal.valueOf(100))
            .build();
    productB =
        Product.builder()
            .id(2L)
            .name("Test product B")
            .description("Test product B description")
            .imageUrl("test_product_b.png")
            .price(BigDecimal.valueOf(200))
            .build();
    cartItemA = CartItem.builder().id(1L).productId(productA.getId()).quantity(2).build();
    cartItemB = CartItem.builder().id(2L).productId(productB.getId()).quantity(10).build();
    order = Order.builder().id(1L).totalPrice(BigDecimal.valueOf(2200)).build();
    orderItemA =
        OrderItem.builder()
            .id(1L)
            .productId(productA.getId())
            .price(BigDecimal.valueOf(100))
            .orderId(order.getId())
            .quantity(2)
            .build();
    orderItemB =
        OrderItem.builder()
            .id(2L)
            .productId(productB.getId())
            .price(BigDecimal.valueOf(200))
            .orderId(order.getId())
            .quantity(10)
            .build();
    orderItemDtoA =
        new OrderItemDto(
            orderItemA.getId(),
            productA.getName(),
            productA.getImageUrl(),
            orderItemA.getQuantity(),
            orderItemA.getPrice());
    orderItemDtoB =
        new OrderItemDto(
            orderItemB.getId(),
            productB.getName(),
            productB.getImageUrl(),
            orderItemB.getQuantity(),
            orderItemB.getPrice());
    cartItemWithProductA =
        CartItemWithProduct.builder().cartItem(cartItemA).product(productA).build();
    cartItemWithProductB =
        CartItemWithProduct.builder().cartItem(cartItemB).product(productB).build();
    orderDto =
        OrderDto.builder()
            .id(1L)
            .totalPrice(order.getTotalPrice())
            .items(List.of(orderItemDtoA, orderItemDtoB))
            .build();
  }

  @Test
  void testCreateOrder_Success() {
    when(cartItemRepository.findAllWithProducts())
        .thenReturn(Flux.just(cartItemWithProductA, cartItemWithProductB));
    when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
    when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.just(orderItemA, orderItemB));
    when(cartItemRepository.deleteAll()).thenReturn(Mono.empty());

    StepVerifier.create(orderService.createOrder())
        .assertNext(
            createdOrder -> {
              assertNotNull(createdOrder, "Объект не должен быть null.");
              assertEquals(order.getId(), createdOrder.getId(), "Id заказа должен быть равен 1.");
              assertEquals(
                  order.getTotalPrice(),
                  createdOrder.getTotalPrice(),
                  "Общая стоимость заказа должна быть равна.");
            })
        .verifyComplete();

    verify(cartItemRepository, times(1)).deleteAll();
    verify(orderRepository, times(1)).save(any(Order.class));
    verify(orderItemRepository, times(1)).saveAll(anyList());
  }

  @Test
  void testCreateOrder_EmptyCart() {
    when(cartItemRepository.findAllWithProducts()).thenReturn(Flux.empty());

    StepVerifier.create(orderService.createOrder())
        .expectErrorMatches(
            throwable ->
                throwable instanceof RuntimeException
                    && throwable.getMessage().equals("Корзина пустая."))
        .verify();

    verify(orderRepository, never()).save(any());
    verify(orderItemRepository, never()).saveAll(anyList());
    verify(cartItemRepository, never()).deleteAll();
  }

  @Test
  void testGetAllOrders() {
    when(orderRepository.findAllOrdersWithItemsAndProducts()).thenReturn(Flux.just(orderDto));

    StepVerifier.create(orderService.getAllOrders())
        .expectNext(orderDto)
        .expectNextCount(0)
        .verifyComplete();
  }

  @Test
  void testGetOrderById_Found() {
    when(orderRepository.findOrderWithItemsAndProducts(1L)).thenReturn(Mono.just(orderDto));

    StepVerifier.create(orderService.getOrderById(1L)).expectNext(orderDto).verifyComplete();

    verify(orderRepository, times(1)).findOrderWithItemsAndProducts(1L);
  }

  @Test
  void testGetOrderById_NotFound() {
    when(orderRepository.findOrderWithItemsAndProducts(1L)).thenReturn(Mono.empty());

    StepVerifier.create(orderService.getOrderById(1L))
        .expectErrorMatches(
            throwable ->
                throwable instanceof RuntimeException
                    && throwable.getMessage().equals("Заказ не найден."))
        .verify();

    verify(orderRepository, times(1)).findOrderWithItemsAndProducts(1L);
  }

  @Test
  void testGetOrdersSummary() {
    when(orderRepository.findAllOrdersWithItemsAndProducts()).thenReturn(Flux.just(orderDto));

    StepVerifier.create(orderService.getOrdersSummary())
        .assertNext(
            ordersSummary -> {
              assertNotNull(ordersSummary, "Объект не должен быть null.");
              assertEquals(
                  BigDecimal.valueOf(2200),
                  ordersSummary.getTotalAmount(),
                  "Общая стоимость всех заказов должна быть равна 2200.");
              assertThat(
                  "Общее количество заказов должно быть равно 1.",
                  ordersSummary.getOrderDtoList(),
                  hasSize(1));
            })
        .verifyComplete();
  }

  @TestConfiguration
  static class MapperTestConfig {
    @Bean
    public OrderMapper orderMapper() {
      return Mappers.getMapper(OrderMapper.class);
    }
  }
}
