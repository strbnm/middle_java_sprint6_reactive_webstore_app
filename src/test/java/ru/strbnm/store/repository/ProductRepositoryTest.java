package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.strbnm.store.testcases.ProductTestCase;
import ru.strbnm.store.utils.ProductSortEnum;

@Slf4j
class ProductRepositoryTest extends BaseR2dbcTest {

  @Autowired private ProductRepository productRepository;

  /*
  После выполнения скрипта INIT_STORE_RECORD.sql таблица products содержит следующие записи:
  id      name                description                             image_url               price
  ==============================================================================================
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
  */

  @ParameterizedTest
  @MethodSource("provideTestCases")
  void findFilteredProducts_shouldReturnAllFilteredProductsSortingAndPaging(ProductTestCase.TestCase testCase) {
    productRepository
        .findFilteredProducts(
            testCase.text(),
            testCase.price_from(),
            testCase.price_to(),
            testCase.letter(),
            testCase.page(),
            testCase.size(),
            testCase.sorting() != null
                ? ProductSortEnum.valueOf(testCase.sorting()).getSortExpression()
                : ProductSortEnum.NAME_ASC.getSortExpression())
        .collectList()
        .as(StepVerifier::create)
        .assertNext(
            filteredProducts -> {
              log.info("filteredProducts={}", filteredProducts);
              assertThat(filteredProducts, hasSize(testCase.countOnPage()));
              assertEquals(testCase.firstProductName(), filteredProducts.getFirst().getName());
              assertEquals(testCase.firstProductPrice(), filteredProducts.getFirst().getPrice());
              assertEquals(testCase.lastProductName(), filteredProducts.getLast().getName());
              assertEquals(testCase.lastProductPrice(), filteredProducts.getLast().getPrice());
            })
        .verifyComplete();
  }

  private Stream<ProductTestCase.TestCase> provideTestCases() {
    return ProductTestCase.provideTestCases();
  }
}
