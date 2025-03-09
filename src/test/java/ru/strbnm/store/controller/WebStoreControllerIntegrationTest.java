package ru.strbnm.store.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.ProductRepository;
import ru.strbnm.store.testcases.ProductTestCase;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebStoreControllerIntegrationTest extends BaseControllerIntegrationTest {
  @Autowired private WebTestClient webTestClient;

  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private ProductRepository productRepository;

  /*
  После выполнения скрипта INIT_STORE_RECORD.sql таблица products содержит следующие записи:
    id      name                description                             image_url               price
    ==================================================================================================
    1       'Eco Gadget 690'    'Description for product_1'             'product_1.png'         464.64
    2       'Ultra Gadget 123'  'Description for product_2'             'product_2.png'         191.38
    3       'Smart Object 598'  'Description for product_3'             'product_3.png'         357.76
    4       'Ultra Widget 734'  'Description for product_4'             'product_4.png'         84.71
    5       'Super Item 561'    'Description for product_5'             'product_5.png'         432.91
    6       'Ultra Item 402'    'Description for product_6'             'product_6.png'         289.61
    7       'Ultra Object 450'  'Description for product_7'             'product_7.png'         31.17
    8       'Ultra Object 457'  'Description for product_8'             'product_8.png'         334.53
    9       'Eco Thing 576'     'Description for product_9'             'product_9.png'         211.65
    10      'Eco Item 319'      'Description for product_10'            'product_10.png'        301.50

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

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void showProductShowcase_shouldReturnHtmlFirstPageWithProductShowcaseFilteredAndSorting(
      ProductTestCase.TestCase testCase) throws Exception {
    String responseBody =
        webTestClient
            .get()
            .uri(
                uriBuilder -> {
                  uriBuilder.path("/products");
                  uriBuilder.queryParam("page", testCase.page());
                  uriBuilder.queryParam("size", testCase.size());
                  if (testCase.text() != null) {
                    uriBuilder.queryParam("text", testCase.text());
                  }
                  if (testCase.letter() != null) {
                    uriBuilder.queryParam("letter", testCase.letter());
                  }
                  if (testCase.price_from() != null) {
                    uriBuilder.queryParam("price_from", String.valueOf(testCase.price_from()));
                  }
                  if (testCase.price_to() != null) {
                    uriBuilder.queryParam("price_to", String.valueOf(testCase.price_to()));
                  }
                  if (testCase.sorting() != null) {
                    uriBuilder.queryParam("sorting", testCase.sorting());
                  }
                  return uriBuilder.build();
                })
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
    // Проверка, что Заголовок страницы "Все товары"
    assertEquals("Все товары", xpath.evaluate("//main/h4", xmlDocument));
    // Проверка, что на странице пять товаров
    assertEquals(
        String.valueOf(testCase.countOnPage()),
        xpath.evaluate("count(//ul[@class='collection']/li)", xmlDocument));
    // Проверяем название и цену для первого товара на странице
    assertEquals(
        testCase.firstProductName(),
        xpath.evaluate("//ul[@class='collection']/li[1]/span[@class='title']", xmlDocument));
    assertEquals(
        "Цена: " + testCase.firstProductPrice() + " руб.",
        xpath.evaluate("//ul[@class='collection']/li[1]/p", xmlDocument));
    // Проверяем название, цену и количество для последнего товара на странице
    assertEquals(
        testCase.lastProductName(),
        xpath.evaluate(
            "//ul[@class='collection']/li[" + testCase.countOnPage() + "]/span[@class='title']",
            xmlDocument));
    assertEquals(
        "Цена: " + testCase.lastProductPrice() + " руб.",
        xpath.evaluate(
            "//ul[@class='collection']/li[" + testCase.countOnPage() + "]/p", xmlDocument));
  }

  @ParameterizedTest
  @CsvSource({
    "1, 'Eco Gadget 690', 'Description for product_1', 464.64, 0",
    "2, 'Ultra Gadget 123', 'Description for product_2', 191.38, 10",
    "3, 'Smart Object 598', 'Description for product_3', 357.76, 0",
    "4, 'Ultra Widget 734', 'Description for product_4', 84.71, 20",
    "5, 'Super Item 561', 'Description for product_5', 432.91, 0",
    "6, 'Ultra Item 402', 'Description for product_6', 289.61, 30",
    "7, 'Ultra Object 450', 'Description for product_7', 31.17, 0",
    "8, 'Ultra Object 457', 'Description for product_8', 334.53, 0",
    "9, 'Eco Thing 576', 'Description for product_9', 211.65, 1",
    "10, 'Eco Item 319', 'Description for product_10', 301.50, 0"
  })
  void getProductById_shouldReturnHtmlWithProductDetail(
      Long productId,
      String productName,
      String productDescription,
      BigDecimal productPrice,
      int productCartQuantity)
      throws Exception {
    String responseBody =
        webTestClient
            .get()
            .uri("/products/" + productId)
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
    // Проверка названия товара
    assertEquals(productName, xpath.evaluate("//h3", xmlDocument));
    // Проверка описания товара
    assertEquals(productDescription, xpath.evaluate("//p", xmlDocument));
    // Проверка цены товара
    assertFalse(
        xpath
            .evaluate("//p[contains(text(), 'Цена: " + productPrice + " руб.')]", xmlDocument)
            .isEmpty());
    // Проверка отображения кнопки "В корзину"
    assertEquals(
        productCartQuantity == 0 ? "" : "display: none;",
        xpath.evaluate(
            "//button[@class='btn add-to-cart'][@data-product-id='" + productId + "']/@style",
            xmlDocument));
    // Проверка отображения блока количества товара
    assertEquals(
        productCartQuantity != 0 ? "" : "display: none;",
        xpath.evaluate("//div[@class='quantity-controls']/@style", xmlDocument));
    // Проверка количества товара в поле input
    assertEquals(
        productCartQuantity != 0 ? String.valueOf(productCartQuantity) : "1",
        xpath.evaluate(
            "//input[@class='quantity-input'][@data-product-id='" + productId + "']/@value",
            xmlDocument));
  }

  private Stream<ProductTestCase.TestCase> provideTestCases() {
    return ProductTestCase.provideTestCases();
  }
}
