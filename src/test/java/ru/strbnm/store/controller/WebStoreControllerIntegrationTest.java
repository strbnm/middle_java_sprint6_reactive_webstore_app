package ru.strbnm.store.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.ProductRepository;

public class WebStoreControllerIntegrationTest extends BaseControllersIntegrationTest {
  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private CartItemRepository cartItemRepository;
  @Autowired private ProductRepository productRepository;

  @Autowired private MockMvc mockMvc;

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
  private record TestCase(
      int page,
      int size,
      String text,
      BigDecimal price_from,
      BigDecimal price_to,
      String letter,
      String sorting,
      int countOnPage,
      String firstProductName,
      BigDecimal firstProductPrice,
      String lastProductName,
      BigDecimal lastProductPrice) {}

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void showProductShowcase_shouldReturnHtmlFirstPageWithProductShowcaseFilteredAndSorting(
      TestCase testCase) throws Exception {
    MockHttpServletRequestBuilder request =
        get("/products")
            .queryParam("page", String.valueOf(testCase.page))
            .queryParam("size", String.valueOf(testCase.size))
            .queryParam("text", testCase.text)
            .queryParam("letter", testCase.letter);

    if (testCase.price_from != null) {
      request.queryParam("price_from", String.valueOf(testCase.price_from));
    }
    if (testCase.price_to != null) {
      request.queryParam("price_to", String.valueOf(testCase.price_to));
    }
    if (testCase.sorting != null) {
      request.queryParam("sorting", testCase.sorting);
    }
    mockMvc
        .perform(request)
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("products/showcase"))
        .andExpect(
            model()
                .attributeExists(
                    "products",
                    "size",
                    "sortOptions",
                    "selectedSorting",
                    "cartItemMap",
                    "cartTotalQuantity",
                    "isShowCase"))
        // Проверка, что Заголовок страницы "Все товары"
        .andExpect(xpath("//main/h4").string("Все товары"))
        // Проверка, что на странице пять товаров
        .andExpect(xpath("//ul[@class='collection']/li").nodeCount(testCase.countOnPage))
        // Проверяем, что в общее количество по всем товарам корзины равно 61 (бейдж рядом с
        // корзиной)
        .andExpect(xpath("//span[@id='cartBadge']").string("61"))
        // Проверяем название и цену для первого товара на странице
        .andExpect(
            xpath("//ul[@class='collection']/li[1]/span[@class='title']")
                .string(testCase.firstProductName))
        .andExpect(
            xpath("//ul[@class='collection']/li[1]/p")
                .string("Цена: " + testCase.firstProductPrice + " руб."))
        // Проверяем название, цену и количество для последнего товара на странице
        .andExpect(
            xpath("//ul[@class='collection']/li[" + testCase.countOnPage + "]/span[@class='title']")
                .string(testCase.lastProductName))
        .andExpect(
            xpath("//ul[@class='collection']/li[" + testCase.countOnPage + "]/p")
                .string("Цена: " + testCase.lastProductPrice + " руб."));
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
    mockMvc
        .perform(get("/products/{id}", productId))
        .andExpect(status().isOk())
        .andExpect(content().contentType("text/html;charset=UTF-8"))
        .andExpect(view().name("products/product_detail"))
        .andExpect(
            productCartQuantity == 0
                ? model().attributeExists("product", "cartTotalQuantity")
                : model().attributeExists("product", "cartTotalQuantity", "cartItem"))
        // Проверка названия товара
        .andExpect(xpath("//h3").string(productName))
        // Проверка описания товара
        .andExpect(xpath("//p").string(productDescription))
        // Проверка цены товара
        .andExpect(xpath("//p[contains(text(), 'Цена: " + productPrice + " руб.')]").exists())
        // Проверка отображения кнопки "В корзину"
        .andExpect(
            xpath("//button[@class='btn add-to-cart'][@data-product-id='" + productId + "']/@style")
                .string(productCartQuantity == 0 ? "" : "display: none;"))
        // Проверка отображения блока количества товара
        .andExpect(
            xpath("//div[@class='quantity-controls']/@style")
                .string(productCartQuantity != 0 ? "" : "display: none;"))
        // Проверка количества товара в поле input
        .andExpect(
            xpath("//input[@class='quantity-input'][@data-product-id='" + productId + "']/@value")
                .string(productCartQuantity != 0 ? String.valueOf(productCartQuantity) : "1"));
  }

  private static Stream<TestCase> provideTestCases() {
    return Stream.of(
        new TestCase(
            0,
            5,
            null,
            null,
            null,
            null,
            null,
            5,
            "Eco Gadget 690",
            new BigDecimal("464.64"),
            "Super Item 561",
            new BigDecimal("432.91")),
        new TestCase(
            1,
            5,
            null,
            null,
            null,
            null,
            null,
            5,
            "Ultra Gadget 123",
            new BigDecimal("191.38"),
            "Ultra Widget 734",
            new BigDecimal("84.71")),
        new TestCase(
            0,
            5,
            null,
            null,
            null,
            null,
            "NAME_ASC",
            5,
            "Eco Gadget 690",
            new BigDecimal("464.64"),
            "Super Item 561",
            new BigDecimal("432.91")),
        new TestCase(
            0,
            5,
            null,
            null,
            null,
            null,
            "NAME_DESC",
            5,
            "Ultra Widget 734",
            new BigDecimal("84.71"),
            "Ultra Gadget 123",
            new BigDecimal("191.38")),
        new TestCase(
            0,
            10,
            null,
            null,
            null,
            null,
            "PRICE_ASC",
            10,
            "Ultra Object 450",
            new BigDecimal("31.17"),
            "Eco Gadget 690",
            new BigDecimal("464.64")),
        new TestCase(
            0,
            10,
            null,
            null,
            null,
            null,
            "PRICE_DESC",
            10,
            "Eco Gadget 690",
            new BigDecimal("464.64"),
            "Ultra Object 450",
            new BigDecimal("31.17")),
        new TestCase(
            0,
            5,
            "Object",
            null,
            null,
            null,
            "NAME_ASC",
            3,
            "Smart Object 598",
            new BigDecimal("357.76"),
            "Ultra Object 457",
            new BigDecimal("334.53")),
        new TestCase(
            0,
            5,
            "Object",
            null,
            null,
            null,
            "NAME_DESC",
            3,
            "Ultra Object 457",
            new BigDecimal("334.53"),
            "Smart Object 598",
            new BigDecimal("357.76")),
        new TestCase(
            0,
            10,
            "Object",
            null,
            null,
            null,
            "PRICE_ASC",
            3,
            "Ultra Object 450",
            new BigDecimal("31.17"),
            "Smart Object 598",
            new BigDecimal("357.76")),
        new TestCase(
            0,
            10,
            "Object",
            null,
            null,
            null,
            "PRICE_DESC",
            3,
            "Smart Object 598",
            new BigDecimal("357.76"),
            "Ultra Object 450",
            new BigDecimal("31.17")),
        new TestCase(
            0,
            5,
            null,
            new BigDecimal("100"),
            new BigDecimal("300"),
            null,
            "NAME_ASC",
            3,
            "Eco Thing 576",
            new BigDecimal("211.65"),
            "Ultra Item 402",
            new BigDecimal("289.61")),
        new TestCase(
            0,
            5,
            null,
            new BigDecimal("100"),
            new BigDecimal("300"),
            null,
            "NAME_DESC",
            3,
            "Ultra Item 402",
            new BigDecimal("289.61"),
            "Eco Thing 576",
            new BigDecimal("211.65")),
        new TestCase(
            0,
            10,
            null,
            new BigDecimal("200"),
            new BigDecimal("400"),
            null,
            "PRICE_ASC",
            5,
            "Eco Thing 576",
            new BigDecimal("211.65"),
            "Smart Object 598",
            new BigDecimal("357.76")),
        new TestCase(
            0,
            10,
            null,
            new BigDecimal("200"),
            new BigDecimal("400"),
            null,
            "PRICE_DESC",
            5,
            "Smart Object 598",
            new BigDecimal("357.76"),
            "Eco Thing 576",
            new BigDecimal("211.65")),
        new TestCase(
            0,
            5,
            "Eco",
            new BigDecimal("100"),
            new BigDecimal("300"),
            null,
            "NAME_ASC",
            1,
            "Eco Thing 576",
            new BigDecimal("211.65"),
            "Eco Thing 576",
            new BigDecimal("211.65")),
        new TestCase(
            0,
            5,
            "Gadget",
            new BigDecimal("100"),
            new BigDecimal("300"),
            null,
            "NAME_DESC",
            1,
            "Ultra Gadget 123",
            new BigDecimal("191.38"),
            "Ultra Gadget 123",
            new BigDecimal("191.38")),
        new TestCase(
            0,
            10,
            "Ultra",
            new BigDecimal("200"),
            new BigDecimal("400"),
            null,
            "PRICE_ASC",
            2,
            "Ultra Item 402",
            new BigDecimal("289.61"),
            "Ultra Object 457",
            new BigDecimal("334.53")),
        new TestCase(
            0,
            10,
            "Ultra",
            new BigDecimal("200"),
            new BigDecimal("400"),
            null,
            "PRICE_DESC",
            2,
            "Ultra Object 457",
            new BigDecimal("334.53"),
            "Ultra Item 402",
            new BigDecimal("289.61")),
        new TestCase(
            0,
            5,
            null,
            null,
            null,
            "U",
            "NAME_ASC",
            5,
            "Ultra Gadget 123",
            new BigDecimal("191.38"),
            "Ultra Widget 734",
            new BigDecimal("84.71")),
        new TestCase(
            0,
            5,
            null,
            null,
            null,
            "E",
            "NAME_DESC",
            3,
            "Eco Thing 576",
            new BigDecimal("211.65"),
            "Eco Gadget 690",
            new BigDecimal("464.64")),
        new TestCase(
            0,
            10,
            null,
            null,
            null,
            "S",
            "PRICE_ASC",
            2,
            "Smart Object 598",
            new BigDecimal("357.76"),
            "Super Item 561",
            new BigDecimal("432.91")),
        new TestCase(
            0,
            10,
            null,
            null,
            null,
            "S",
            "PRICE_DESC",
            2,
            "Super Item 561",
            new BigDecimal("432.91"),
            "Smart Object 598",
            new BigDecimal("357.76")));
  }
}
