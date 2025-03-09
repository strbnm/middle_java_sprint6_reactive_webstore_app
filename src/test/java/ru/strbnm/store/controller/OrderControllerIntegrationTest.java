package ru.strbnm.store.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.StringReader;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import reactor.test.StepVerifier;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.OrderItemRepository;
import ru.strbnm.store.repository.OrderRepository;

public class OrderControllerIntegrationTest extends BaseControllerIntegrationTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private OrderItemRepository orderItemRepository;
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
    StepVerifier.create(orderRepository.findAll().collectList())
        .assertNext(
            orders ->
                assertThat(
                    "Количество заказов перед оформлением нового заказа должно быть равно 3.",
                    orders,
                    hasSize(3)))
        .verifyComplete();

    StepVerifier.create(orderItemRepository.findAll().collectList())
        .assertNext(
            orderItems ->
                assertThat(
                    "Количество позиций заказов перед оформлением нового заказа должно быть равно 6.",
                    orderItems,
                    hasSize(6)))
        .verifyComplete();

    StepVerifier.create(cartItemRepository.findAll().collectList())
        .assertNext(
            cartItems ->
                assertThat(
                    "Количество позиций корзины перед оформлением нового заказа должно быть равно 4.",
                    cartItems,
                    hasSize(4)))
        .verifyComplete();

    webTestClient
        .post()
        .uri("/orders/checkout")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .exchange()
        .expectStatus()
        .is3xxRedirection()
        .expectHeader()
        .valueEquals("Location", "/orders/4");
    StepVerifier.create(orderRepository.findAll().collectList())
        .assertNext(
            orders ->
                assertThat(
                    "Количество заказов после оформления заказа должно быть равно 4.",
                    orders,
                    hasSize(4)))
        .verifyComplete();

    StepVerifier.create(orderItemRepository.findAll().collectList())
        .assertNext(
            orderItems ->
                assertThat(
                    "Количество позиций заказов после оформления заказа должно быть равно 10.",
                    orderItems,
                    hasSize(10)))
        .verifyComplete();

    StepVerifier.create(cartItemRepository.findAll().collectList())
        .assertNext(
            cartItems ->
                assertThat(
                    "Количество позиций корзины после оформления заказа должно быть равно 0.",
                    cartItems,
                    hasSize(0)))
        .verifyComplete();

    StepVerifier.create(orderRepository.findOrderWithItemsAndProducts(4L))
        .assertNext(
            createdOrder -> {
              assertNotNull(createdOrder, "Возвращаемый объект не должен быть null.");
              assertEquals(
                  BigDecimal.valueOf(12507.95),
                  createdOrder.getTotalPrice(),
                  "Общая стоимость заказа с id=4 должна быть равна 12507.95.");
              assertThat(
                  "Количество наименований товаров в заказе должно быть 4.",
                  createdOrder.getItems(),
                  hasSize(4));
            })
        .verifyComplete();
  }

  @Test
  void getOrders_shouldReturnHtmlWithListOrders() throws Exception {
    String responseBody =
        webTestClient
            .get()
            .uri("/orders")
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType("text/html")
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseBody);

    // Парсим HTML как XML
    InputSource source = new InputSource(new StringReader(responseBody));
    Document xmlDocument = documentBuilder.parse(source);
    // Проверка, что Заголовок страницы "Все заказы"
    assertEquals("Все заказы", xpath.evaluate("//main/h4", xmlDocument));
    // Проверяем, что на странице представлено 3 заказа
    assertEquals("3", xpath.evaluate("count(//ul[@class='collection']/li)", xmlDocument));
    // Проверяем название, цену и количество для первого заказа
    assertEquals(
        "Заказ #1",
        xpath.evaluate("//ul[@class='collection']/li[1]/span[@class='title']", xmlDocument));
    assertEquals(
        "Количество позиций в заказе: 2",
        xpath.evaluate("//ul[@class='collection']/li[1]/p[1]", xmlDocument));
    assertEquals(
        "Сумма заказа: 6319.05 руб.",
        xpath.evaluate("//ul[@class='collection']/li[1]/p[2]", xmlDocument));
    // Проверяем название, цену и количество для второго заказа
    assertEquals(
        "Заказ #2",
        xpath.evaluate("//ul[@class='collection']/li[2]/span[@class='title']", xmlDocument));
    assertEquals(
        "Количество позиций в заказе: 1",
        xpath.evaluate("//ul[@class='collection']/li[2]/p[1]", xmlDocument));
    assertEquals(
        "Сумма заказа: 677.68 руб.",
        xpath.evaluate("//ul[@class='collection']/li[2]/p[2]", xmlDocument));
    // Проверяем название, цену и количество для третьего заказа
    assertEquals(
        "Заказ #3",
        xpath.evaluate("//ul[@class='collection']/li[3]/span[@class='title']", xmlDocument));
    assertEquals(
        "Количество позиций в заказе: 3",
        xpath.evaluate("//ul[@class='collection']/li[3]/p[1]", xmlDocument));
    assertEquals(
        "Сумма заказа: 6210.91 руб.",
        xpath.evaluate("//ul[@class='collection']/li[3]/p[2]", xmlDocument));
  }

  @Test
  void getOrderById_shouldReturnHtmlWithOrderDetail() throws Exception {
    long orderId = 1L;
    String responseBody =
        webTestClient
            .get()
            .uri("/orders/" + orderId)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType("text/html")
            .expectBody(String.class)
            .returnResult()
            .getResponseBody();

    assertNotNull(responseBody);

    // Парсим HTML как XML
    InputSource source = new InputSource(new StringReader(responseBody));
    Document xmlDocument = documentBuilder.parse(source);
    // Проверка, что Заголовок страницы "Состав заказа #1"
    assertEquals("Состав заказа #" + orderId, xpath.evaluate("//main/h4", xmlDocument));
    // Проверяем, что на странице представлено 2 позиции товара в заказе 1
    assertEquals("2", xpath.evaluate("count(//ul[@class='collection']/li)", xmlDocument));
    // Проверяем название, цену и количество товара для первой позиции заказа
    assertEquals(
        "Eco Gadget 690",
        xpath.evaluate("//ul[@class='collection']/li[1]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: 464.64 руб.", xpath.evaluate("//ul[@class='collection']/li[1]/p[1]", xmlDocument));
    assertEquals(
        "Количество: 10", xpath.evaluate("//ul[@class='collection']/li[1]/p[2]", xmlDocument));
    // Проверяем название, цену и количество товара для второй позиции заказа
    assertEquals(
        "Ultra Object 457",
        xpath.evaluate("//ul[@class='collection']/li[2]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: 334.53 руб.", xpath.evaluate("//ul[@class='collection']/li[2]/p[1]", xmlDocument));
    assertEquals(
        "Количество: 5", xpath.evaluate("//ul[@class='collection']/li[2]/p[2]", xmlDocument));
  }
}
