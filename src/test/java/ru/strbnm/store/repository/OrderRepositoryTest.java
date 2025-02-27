package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.strbnm.store.entity.Order;

class OrderRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

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
        Order order = orderRepository.findById(1L).orElse(null);
        assertNotNull(order, "Объект заказа не должен быть null.");
        assertEquals(order.getId(), 1L, "Id заказа должен быть равен 1.");
        assertEquals(order.getTotalPrice(), BigDecimal.valueOf(6319.05), "Общая сумма заказа с id=1 должна быть равна 6319.05.");
        assertThat("Заказ с id=1 должен иметь 2 позиции", order.getItems(), hasSize(2));
    }

    @Test
    void testFindOrderWithItemsAndProducts() {
        Optional<Order> foundOrder = orderRepository.findOrderWithItemsAndProducts(1L);

        assertTrue(foundOrder.isPresent(), "Объект заказа не должен быть null.");
        assertFalse(foundOrder.get().getItems().isEmpty(), "Список позиций заказа не должен быть пуст.");
        assertNotNull(foundOrder.get().getItems().getFirst().getProduct(), "Объект товара в списке позиций заказа не должен быть null.");
    }

    @Test
    void findAll_shouldReturnAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<BigDecimal> totalPriceList = orders.stream().map(Order::getTotalPrice).toList();

        assertThat("Количество записей должно быть равно 3.", orders, hasSize(3));
        assertEquals(totalPriceList, List.of(BigDecimal.valueOf(6319.05), BigDecimal.valueOf(677.68), BigDecimal.valueOf(6210.91)));
    }
}