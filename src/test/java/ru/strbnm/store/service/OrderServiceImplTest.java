package ru.strbnm.store.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderSummaryDto;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Order;
import ru.strbnm.store.entity.OrderItem;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.OrderItemMapper;
import ru.strbnm.store.mapper.OrderMapper;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.OrderItemRepository;
import ru.strbnm.store.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {OrderServiceImpl.class, OrderServiceImplTest.MapperTestConfig.class})
public class OrderServiceImplTest {

  @MockitoBean private OrderRepository orderRepository;

  @MockitoBean private OrderItemRepository orderItemRepository;

  @MockitoBean private CartItemRepository cartItemRepository;

  @Autowired private OrderMapper orderMapper;
  @Autowired private OrderItemMapper orderItemMapper;

  @Autowired private OrderServiceImpl orderService;

  private Product productA;
  private Product productB;
  private CartItem cartItemA;
  private CartItem cartItemB;
  private Order order;
  private OrderItem orderItemA;
  private OrderItem orderItemB;

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
    cartItemA = CartItem.builder().id(1L).product(productA).quantity(2).build();
    cartItemB = CartItem.builder().id(2L).product(productB).quantity(10).build();
    order = Order.builder().id(1L).totalPrice(BigDecimal.valueOf(2200)).build();
    orderItemA =
        OrderItem.builder()
            .id(1L)
            .product(productA)
            .price(BigDecimal.valueOf(100))
            .order(order)
            .quantity(2)
            .build();
    orderItemB =
        OrderItem.builder()
            .id(2L)
            .product(productB)
            .price(BigDecimal.valueOf(200))
            .order(order)
            .quantity(10)
            .build();
  }

  @Test
  void testCreateOrder_Success() {
    when(cartItemRepository.findAll()).thenReturn(List.of(cartItemA, cartItemB));
    when(orderRepository.save(any(Order.class))).thenReturn(order);
    when(orderItemRepository.saveAll(anyList())).thenReturn(List.of(orderItemA, orderItemB));

    OrderDto result = orderService.createOrder();

    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(order.getId(), result.getId(), "Id заказа должен быть равен 1.");
    assertEquals(
        order.getTotalPrice(), result.getTotalPrice(), "Общая стоимость заказа должна быть равна.");

    verify(cartItemRepository, times(1)).deleteAll();
    verify(orderRepository, times(1)).save(any(Order.class));
    verify(orderItemRepository, times(1)).saveAll(anyList());
  }

  @Test
  void testCreateOrder_EmptyCart() {
    when(cartItemRepository.findAll()).thenReturn(List.of());

    Exception exception = assertThrows(RuntimeException.class, () -> orderService.createOrder());
    assertEquals("Корзина пустая.", exception.getMessage());
  }

  @Test
  void testGetAllOrders() {
    when(orderRepository.findAll()).thenReturn(List.of(order));

    List<OrderDto> result = orderService.getAllOrders();

    assertNotNull(result, "Объект не должен быть null.");
    assertThat("Количество заказов должно быть равно 1.", result, hasSize(1));
  }

  @Test
  void testGetOrderById_Found() {
    when(orderRepository.findOrderWithItemsAndProducts(1L)).thenReturn(Optional.of(order));

    OrderDto result = orderService.getOrderById(1L);

    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(order.getId(), result.getId());

    verify(orderRepository, times(1)).findOrderWithItemsAndProducts(1L);
  }

  @Test
  void testGetOrderById_NotFound() {
    when(orderRepository.findOrderWithItemsAndProducts(1L)).thenReturn(Optional.empty());

    Exception exception = assertThrows(RuntimeException.class, () -> orderService.getOrderById(1L));
    assertEquals("Заказ не найден.", exception.getMessage());

    verify(orderRepository, times(1)).findOrderWithItemsAndProducts(1L);
  }

  @Test
  void testGetOrdersSummary() {
    when(orderRepository.findAll()).thenReturn(List.of(order));

    OrderSummaryDto result = orderService.getOrdersSummary();

    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(
        BigDecimal.valueOf(2200),
        result.getTotalAmount(),
        "Общая стоимость всех заказов должна быть равна 2200.");
    assertThat(
        "Общее количество заказов должно быть равно 1.", result.getOrderDtoList(), hasSize(1));
  }

  @TestConfiguration
  static class MapperTestConfig {
    @Bean
    public OrderMapper orderMapper() {
      return Mappers.getMapper(OrderMapper.class);
    }

    @Bean
    public OrderItemMapper orderItemMapper() {
      return Mappers.getMapper(OrderItemMapper.class);
    }
  }
}
