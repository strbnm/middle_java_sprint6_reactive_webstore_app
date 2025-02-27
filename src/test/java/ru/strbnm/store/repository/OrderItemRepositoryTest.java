package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.strbnm.store.entity.Order;
import ru.strbnm.store.entity.OrderItem;
import ru.strbnm.store.entity.Product;

public class OrderItemRepositoryTest extends BaseRepositoryTest {
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private ProductRepository productRepository;
  @Autowired private OrderRepository orderRepository;

  /*
   После выполнения скрипта INIT_STORE_RECORD.sql таблица orders-items содержит следующие записи:
   id  product_id  quantity    price   order_id
   ============================================
   1   1           10          464.64  1
   2   8           5           334.53  1
   3   4           2           84.71   2
   4   7           3           31.17   3
   5   3           10          357.76  3
   6   9           12          211.65  3
  */

  private record OrderItemRecord(Long product_id, int quantity, BigDecimal price, Long order_id) {}

  @Test
  void findAll_shouldReturnAllOrderItems() {
    HashMap<Long, OrderItemRecord> expectedOrderItems = new HashMap<>();
    expectedOrderItems.put(1L, new OrderItemRecord(1L, 10, BigDecimal.valueOf(464.64), 1L));
    expectedOrderItems.put(2L, new OrderItemRecord(8L, 5, BigDecimal.valueOf(334.53), 1L));
    expectedOrderItems.put(3L, new OrderItemRecord(4L, 2, BigDecimal.valueOf(84.71), 2L));
    expectedOrderItems.put(4L, new OrderItemRecord(7L, 3, BigDecimal.valueOf(31.17), 3L));
    expectedOrderItems.put(5L, new OrderItemRecord(3L, 10, BigDecimal.valueOf(357.76), 3L));
    expectedOrderItems.put(6L, new OrderItemRecord(9L, 12, BigDecimal.valueOf(211.65), 3L));

    List<OrderItem> orderItems = orderItemRepository.findAll();

    HashMap<Long, OrderItemRecord> actualOrderItems = new HashMap<>();
    for (OrderItem orderItem : orderItems) {
      actualOrderItems.put(
          orderItem.getId(),
          new OrderItemRecord(
              orderItem.getProduct().getId(),
              orderItem.getQuantity(),
              orderItem.getPrice(),
              orderItem.getOrder().getId()));
    }

    assertThat("Должно быть 6 позиций товаров во всех заказах.", orderItems, hasSize(6));
    assertEquals(
        expectedOrderItems,
        actualOrderItems,
        "Ожидаемые и полученные позиции товаров в заказах должны быть равны.");
  }

  @Test
  void save_shouldAddOrderItemToDatabase() {
    Product product =
        productRepository.findById(5L).orElseThrow(() -> new RuntimeException("Товар не найден"));
    Order order =
        orderRepository.findById(1L).orElseThrow(() -> new RuntimeException("Заказ не найден"));
    OrderItem orderItem =
        OrderItem.builder()
            .product(product)
            .quantity(15)
            .price(product.getPrice())
            .order(order)
            .build();

    orderItemRepository.save(orderItem);

    OrderItem savedOrderItem =
        orderItemRepository.findAll().stream()
            .filter(createdCartItem -> createdCartItem.getId().equals(7L))
            .findFirst()
            .orElse(null);
    assertNotNull(savedOrderItem, "Полученный из БД объект не должен быть null.");
    assertEquals(
        "Description for product_5",
        savedOrderItem.getProduct().getDescription(),
        "Описание товара должно быть 'Description for product_5'.");
    assertEquals(15, savedOrderItem.getQuantity(), "Количество товара быть равно 15.");
    assertEquals(
        1L, savedOrderItem.getOrder().getId(), "Идентификатор заказа должен быть равен 1.");
  }
}
