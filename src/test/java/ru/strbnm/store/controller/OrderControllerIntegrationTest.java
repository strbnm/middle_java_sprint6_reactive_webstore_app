package ru.strbnm.store.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Order;
import ru.strbnm.store.entity.OrderItem;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.OrderItemRepository;
import ru.strbnm.store.repository.OrderRepository;

public class OrderControllerIntegrationTest extends BaseControllersIntegrationTest {
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private OrderItemRepository orderItemRepository;
  @Autowired private OrderRepository orderRepository;

  @Autowired private MockMvc mockMvc;

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

   После выполнения скрипта INIT_STORE_RECORD.sql таблица cart-items содержит следующие записи:
   id  product_id  quantity
   ========================
   1   2           10
   2   4           20
   3   6           30
   4   9           1

   Общее кол-во всех товаров в корзине (сумма quantity): 61.
   Общая стоимость товаров в корзине: 12507.95.
  */

  @Test
  void checkoutCartAndCreateOrder_shouldCreateOrderInDatabaseAndRedirect() throws Exception {
    List<Order> ordersBeforeCreateOrder = orderRepository.findAll();
    List<OrderItem> orderItemsBeforeCreateOrder = orderItemRepository.findAll();
    List<CartItem> cartItemsBeforeCreateOrder = cartItemRepository.findAll();
    assertThat(
        "Количество заказов перед оформлением нового заказа должно быть равно 3.",
        ordersBeforeCreateOrder,
        hasSize(3));
    assertThat(
        "Количество позиций заказов перед оформлением нового заказа должно быть равно 6.",
        orderItemsBeforeCreateOrder,
        hasSize(6));
    assertThat(
        "Количество позиций корзины перед оформлением нового заказа должно быть равно 4.",
        cartItemsBeforeCreateOrder,
        hasSize(4));

    mockMvc
        .perform(post("/orders/checkout").contentType("application/x-www-form-urlencoded"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/orders/4"));

    List<Order> ordersAfterCreateOrder = orderRepository.findAll();
    List<OrderItem> orderItemsAfterCreateOrder = orderItemRepository.findAll();
    List<CartItem> cartItemsAfterCreateOrder = cartItemRepository.findAll();
    assertThat(
        "Количество заказов после оформления заказа должно быть равно 4.",
        ordersAfterCreateOrder,
        hasSize(4));
    assertThat(
        "Количество позиций заказов после оформления заказа должно быть равно 10.",
        orderItemsAfterCreateOrder,
        hasSize(10));
    assertThat(
        "Количество позиций корзины после оформления заказа должно быть равно 0.",
        cartItemsAfterCreateOrder,
        hasSize(0));

    Order createdOrder = orderRepository.findById(4L).orElse(null);
    assertNotNull(createdOrder, "Возвращаемый объект не должен быть null.");
    assertEquals(
        BigDecimal.valueOf(12507.95),
        createdOrder.getTotalPrice(),
        "Общая стоимость заказа с id=4 должна быть равна 12507.95.");
    assertThat(
        "Количество наименований товаров в заказе должно быть 4.",
        createdOrder.getItems(),
        hasSize(4));
  }

  @Test
  void getOrders_shouldReturnHtmlWithListOrders() throws Exception {
    mockMvc
        .perform(get("/orders").contentType("text/html;charset=UTF-8"))
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("orders/index"))
        .andExpect(model().attributeExists("orders", "cartTotalQuantity", "totalOrdersAmount"))
        // Проверка, что Заголовок страницы "Все заказы"
        .andExpect(xpath("//main/h4").string("Все заказы"))
        // Проверяем, что на странице представлено 3 заказа
        .andExpect(xpath("//ul[@class='collection']/li").nodeCount(3))
        // Проверяем название, цену и количество для первого заказа
        .andExpect(xpath("//ul[@class='collection']/li[1]/span[@class='title']").string("Заказ #1"))
        .andExpect(
            xpath("//ul[@class='collection']/li[1]/p[1]").string("Количество позиций в заказе: 2"))
        .andExpect(
            xpath("//ul[@class='collection']/li[1]/p[2]").string("Сумма заказа: 6319.05 руб."))
        // Проверяем название, цену и количество для второго заказа
        .andExpect(xpath("//ul[@class='collection']/li[2]/span[@class='title']").string("Заказ #2"))
        .andExpect(
            xpath("//ul[@class='collection']/li[2]/p[1]").string("Количество позиций в заказе: 1"))
        .andExpect(
            xpath("//ul[@class='collection']/li[2]/p[2]").string("Сумма заказа: 677.68 руб."))
        // Проверяем название, цену и количество для третьего заказа
        .andExpect(xpath("//ul[@class='collection']/li[3]/span[@class='title']").string("Заказ #3"))
        .andExpect(
            xpath("//ul[@class='collection']/li[3]/p[1]").string("Количество позиций в заказе: 3"))
        .andExpect(
            xpath("//ul[@class='collection']/li[3]/p[2]").string("Сумма заказа: 6210.91 руб."));
  }

  @Test
  void getOrderById_shouldReturnHtmlWithOrderDetail() throws Exception {
    long orderId = 1L;
    mockMvc
        .perform(get("/orders/" + orderId).contentType("text/html;charset=UTF-8"))
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("orders/order_detail"))
        .andExpect(model().attributeExists("order", "cartTotalQuantity", "title"))
        // Проверка, что Заголовок страницы "Состав заказа #1"
        .andExpect(xpath("//main/h4").string("Состав заказа #" + orderId))
        // Проверяем, что на странице представлено 2 позиции товара в заказе 1
        .andExpect(xpath("//ul[@class='collection']/li").nodeCount(2))
        // Проверяем название, цену и количество товара для первой позиции заказа
        .andExpect(
            xpath("//ul[@class='collection']/li[1]/span[@class='title']").string("Eco Gadget 690"))
        .andExpect(xpath("//ul[@class='collection']/li[1]/p[1]").string("Цена: 464.64 руб."))
        .andExpect(xpath("//ul[@class='collection']/li[1]/p[2]").string("Количество: 10"))
        // Проверяем название, цену и количество товара для второй позиции заказа
        .andExpect(
            xpath("//ul[@class='collection']/li[2]/span[@class='title']")
                .string("Ultra Object 457"))
        .andExpect(xpath("//ul[@class='collection']/li[2]/p[1]").string("Цена: 334.53 руб."))
        .andExpect(xpath("//ul[@class='collection']/li[2]/p[2]").string("Количество: 5"));
  }
}
