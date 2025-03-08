package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.strbnm.store.dto.OrderItemDto;
import ru.strbnm.store.entity.Order;

class OrderRepositoryTest extends BaseR2dbcTest {

  @Autowired private OrderRepository orderRepository;

  /*
   После выполнения скрипта INIT_STORE_RECORD.sql таблица orders содержит следующие записи:
    id       total_price
    ====================
    1       6319.05
    2       677.68
    3       6210.91

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

  @Test
  void findById_shouldReturnOrderById() {
    orderRepository
        .findById(1L)
        .as(StepVerifier::create)
        .assertNext(
            order -> {
              assertNotNull(order, "Объект заказа не должен быть null.");
              assertEquals(order.getId(), 1L, "Id заказа должен быть равен 1.");
              assertEquals(
                  order.getTotalPrice(),
                  BigDecimal.valueOf(6319.05),
                  "Общая сумма заказа с id=1 должна быть равна 6319.05.");
            })
        .verifyComplete();
  }

  @Test
  void testFindOrderWithItemsAndProducts() {
    orderRepository
        .findOrderWithItemsAndProducts(1L)
        .as(StepVerifier::create)
        .assertNext(
            foundOrder -> {
              assertNotNull(foundOrder, "Объект заказа c id=1 не должен быть null.");
              assertFalse(
                  foundOrder.getItems().isEmpty(), "Список позиций заказа не должен быть пуст.");
              assertThat("В заказе должно быть две позиции.", foundOrder.getItems(), hasSize(2));
              assertEquals(
                  foundOrder.getItems().stream().map(OrderItemDto::getProductId).toList(),
                  List.of(1L, 8L),
                  "Список позиций в заказе содержит товары с id 1 и 8.");
            })
        .verifyComplete();
  }

  @Test
  void findAll_shouldReturnAllOrders() {
    orderRepository
        .findAll()
        .collectList()
        .as(StepVerifier::create)
        .assertNext(
            orders -> {
              List<BigDecimal> totalPriceList = orders.stream().map(Order::getTotalPrice).toList();
              assertThat("Количество записей должно быть равно 3.", orders, hasSize(3));
              assertEquals(
                  totalPriceList,
                  List.of(
                      BigDecimal.valueOf(6319.05),
                      BigDecimal.valueOf(677.68),
                      BigDecimal.valueOf(6210.91)));
            })
        .verifyComplete();
  }
}
