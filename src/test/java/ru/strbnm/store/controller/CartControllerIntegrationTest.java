package ru.strbnm.store.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.repository.CartItemRepository;

public class CartControllerIntegrationTest extends BaseControllerIntegrationTest {

  @Autowired private WebTestClient webTestClient;

  @Autowired private CartItemRepository cartItemRepository;

  private CartItemDto cartItemDtoA, cartItemDtoB, cartItemDtoC;

  /*
   После выполнения скрипта INIT_STORE_RECORD.sql таблица cart-items содержит следующие записи:
   id  product_id  quantity
   ========================
   1   2           10
   2   4           20
   3   6           30
   4   9           1

   Общее кол-во всех товаров в корзине (сумма quantity) - 61.
  */

  @BeforeEach
  void setUp() {
    cartItemDtoA = CartItemDto.builder().productId(1L).quantity(5).build();
    cartItemDtoB = CartItemDto.builder().id(1L).productId(2L).quantity(11).build();
    cartItemDtoC = CartItemDto.builder().id(1L).productId(2L).quantity(0).build();
  }

  @Test
  void addToCart_shouldAddProductToCartItemInDatabaseAndReturnCartItemInfoDto() {
    List<CartItem> cartItemsBeforeAdd = cartItemRepository.findAll().collectList().block();
    assertThat("Позиций в корзине до добавления должно быть 4.", cartItemsBeforeAdd, hasSize(4));
    List<Long> oldProductIds = cartItemsBeforeAdd.stream().map(CartItem::getProductId).toList();

    webTestClient
        .post()
        .uri("/cart")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(cartItemDtoA)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.cartItem.id")
        .isEqualTo(5L)
        .jsonPath("$.cartItem.productId")
        .isEqualTo(cartItemDtoA.getProductId())
        .jsonPath("$.cartItem.quantity")
        .isEqualTo(cartItemDtoA.getQuantity())
        .jsonPath("$.cartItemsCount")
        .isEqualTo(66);

    List<CartItem> cartItemsAfterAdd = cartItemRepository.findAll().collectList().block();
    assertThat("Позиций в корзине после добавления должно быть 5.", cartItemsAfterAdd, hasSize(5));

    List<Long> newProductIds = cartItemsAfterAdd.stream().map(CartItem::getProductId).toList();

    assertThat(
        newProductIds,
        allOf(hasItems(oldProductIds.toArray(new Long[0])), hasItem(cartItemDtoA.getProductId())));
  }

  @Test
  void updateCartItem_shouldUpdateCartItemInDatabaseAndReturnCartItemInfoDto() throws Exception {
    List<CartItem> cartItemsBeforeUpdate = cartItemRepository.findAll().collectList().block();
    assertThat("Позиций в корзине до обновления должно быть 4.", cartItemsBeforeUpdate, hasSize(4));
    List<Long> oldProductIds = cartItemsBeforeUpdate.stream().map(CartItem::getProductId).toList();

    webTestClient
        .put()
        .uri("/cart")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(cartItemDtoB)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.cartItem.id")
        .isEqualTo(cartItemDtoB.getId())
        .jsonPath("$.cartItem.productId")
        .isEqualTo(cartItemDtoB.getProductId())
        .jsonPath("$.cartItem.quantity")
        .isEqualTo(cartItemDtoB.getQuantity())
        .jsonPath("$.cartItemsCount")
        .isEqualTo(62);

    List<CartItem> cartItemsAfterUpdate = cartItemRepository.findAll().collectList().block();
    assertThat(
        "Позиций в корзине после обновления должно быть 4.", cartItemsAfterUpdate, hasSize(4));
    List<Long> newProductIds = cartItemsAfterUpdate.stream().map(CartItem::getProductId).toList();
    assertThat(newProductIds, hasItems(oldProductIds.toArray(new Long[0])));
  }

  @Test
  void updateCartItemWithZeroQuantity_shouldDeleteCartItemInDatabaseAndReturnCartItemInfoDto()
      throws Exception {
    List<CartItem> cartItemsBeforeUpdate = cartItemRepository.findAll().collectList().block();
    assertThat("Позиций в корзине до обновления должно быть 4.", cartItemsBeforeUpdate, hasSize(4));

    webTestClient
        .put()
        .uri("/cart")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(cartItemDtoC)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.cartItem.id")
        .isEqualTo(cartItemDtoC.getId())
        .jsonPath("$.cartItem.productId")
        .isEqualTo(cartItemDtoC.getProductId())
        .jsonPath("$.cartItem.quantity")
        .isEqualTo(cartItemDtoC.getQuantity())
        .jsonPath("$.cartItemsCount")
        .isEqualTo(51);

    List<CartItem> cartItemsAfterUpdate = cartItemRepository.findAll().collectList().block();
    assertThat(
        "Позиций в корзине после обновления должно быть 3.", cartItemsAfterUpdate, hasSize(3));
    CartItem deletedCartItem =
        cartItemsAfterUpdate.stream()
            .filter(cartItem -> cartItem.getId().equals(cartItemDtoC.getId()))
            .findFirst()
            .orElse(null);
    assertNull(deletedCartItem, "Объект должен быть null.");
  }

  @Test
  void removeCartItem_shouldDeleteCartItemInDatabaseAndReturnCartItemInfoDto() throws Exception {
    List<CartItem> cartItemsBeforeDelete = cartItemRepository.findAll().collectList().block();
    assertThat("Позиций в корзине до обновления должно быть 4.", cartItemsBeforeDelete, hasSize(4));

    webTestClient
        .method(HttpMethod.DELETE)
        .uri("/cart/" + cartItemDtoB.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus()
        .isOk()
        .expectHeader()
        .contentType(MediaType.APPLICATION_JSON)
        .expectBody()
        .jsonPath("$.cartItemsCount")
        .isEqualTo(51);

    List<CartItem> cartItemsAfterDelete = cartItemRepository.findAll().collectList().block();
    assertThat(
        "Позиций в корзине после обновления должно быть 3.", cartItemsAfterDelete, hasSize(3));
    CartItem deletedCartItem =
        cartItemsAfterDelete.stream()
            .filter(cartItem -> cartItem.getId().equals(cartItemDtoB.getId()))
            .findFirst()
            .orElse(null);
    assertNull(deletedCartItem, "Объект должен быть null.");
  }

  @Test
  void getCart_shouldReturnHtmlWithCart() throws Exception {
    String responseBody =
        webTestClient
            .get()
            .uri("/cart")
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

    // Проверка заголовка страницы
    assertEquals("Товары в корзине", xpath.evaluate("//main/h4", xmlDocument));

    // Проверка количества товаров в корзине
    assertEquals("4", xpath.evaluate("count(//ul[@class='collection']/li)", xmlDocument));

    // Проверка первого товара
    assertEquals(
        "Ultra Gadget 123",
        xpath.evaluate("//ul[@class='collection']/li[1]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: 191.38 руб.", xpath.evaluate("//ul[@class='collection']/li[1]/p", xmlDocument));
    assertEquals(
        "10",
        xpath.evaluate(
            "//ul[@class='collection']/li[1]//input[@class='quantity-input']/@value", xmlDocument));

    // Проверка второго товара
    assertEquals(
        "Ultra Widget 734",
        xpath.evaluate("//ul[@class='collection']/li[2]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: 84.71 руб.", xpath.evaluate("//ul[@class='collection']/li[2]/p", xmlDocument));
    assertEquals(
        "20",
        xpath.evaluate(
            "//ul[@class='collection']/li[2]//input[@class='quantity-input']/@value", xmlDocument));

    // Проверка третьего товара
    assertEquals(
        "Ultra Item 402",
        xpath.evaluate("//ul[@class='collection']/li[3]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: 289.61 руб.", xpath.evaluate("//ul[@class='collection']/li[3]/p", xmlDocument));
    assertEquals(
        "30",
        xpath.evaluate(
            "//ul[@class='collection']/li[3]//input[@class='quantity-input']/@value", xmlDocument));

    // Проверка четвертого товара
    assertEquals(
        "Eco Thing 576",
        xpath.evaluate("//ul[@class='collection']/li[4]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: 211.65 руб.", xpath.evaluate("//ul[@class='collection']/li[4]/p", xmlDocument));
    assertEquals(
        "1",
        xpath.evaluate(
            "//ul[@class='collection']/li[4]//input[@class='quantity-input']/@value", xmlDocument));
  }

  //
  @Test
  void getCartWithoutCartItem_shouldReturnHtmlWithEmptyCart() throws Exception {
    cartItemRepository.deleteAll().block();

    String responseBody =
        webTestClient
            .get()
            .uri("/cart")
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

    assertEquals("Корзина пустая", xpath.evaluate("//main/h4", xmlDocument));
    assertTrue(xpath.evaluate("//ul[@class='collection']/li", xmlDocument).isEmpty());
  }
}
