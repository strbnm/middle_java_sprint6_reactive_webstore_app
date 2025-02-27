package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.repository.spec.ProductSpecifications;

class ProductRepositoryTest extends BaseRepositoryTest {

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

  @Test
  void findById_shouldReturnProductById() {
    Product foundProduct = productRepository.findById(1L).orElse(null);

    assertNotNull(foundProduct, "Объект товара не должен быть null.");
    assertEquals(foundProduct.getId(), 1L, "Id товара должен быть равен 1.");
    assertEquals(
        foundProduct.getName(),
        "Eco Gadget 690",
        "Название товара с id=1 должно быть 'Eco Gadget 690'.");
  }

  @Test
  void FindAll_shouldReturnAllProductsByPageAndSortByIdDesc() {
    Page<Product> products =
        productRepository.findAll(PageRequest.of(0, 5, Sort.by("id").descending()));
    List<Long> actualProductIds = products.getContent().stream().map(Product::getId).toList();

    assertEquals(
        10L, products.getTotalElements(), "Общее количество товаров должно быть равно 10.");
    assertEquals(
        2, products.getTotalPages(), "Общее количество страниц постов должно быть равно 2.");
    assertEquals(
        actualProductIds, List.of(10L, 9L, 8L, 7L, 6L), "В списке id должны быть 10, 9, 8, 7 и 6.");
    assertEquals(
        products.getContent().getFirst().getName(),
        "Eco Item 319",
        "Первым в списке должен быть товар с названием 'Eco Item 319Eco Item 319'.");
  }

  @Test
  void deleteById_shouldDeleteProductFromDatabase() {
    productRepository.deleteById(5L);
    List<Product> products = productRepository.findAll();
    List<Product> deletedProducts =
        products.stream().filter(post -> post.getId().equals(5L)).toList();

    assertThat("Должно быть 9 товаров.", products, hasSize(9));
    assertTrue(
        deletedProducts.isEmpty(), "Должен быть пустой список результатов для товаров с id=5.");
  }

  // Тесты ProductSpecifications
  @Test
  void testFindByNameOrDescriptionContains() {
    Specification<Product> spec = ProductSpecifications.nameOrDescriptionContains("Gadget");
    List<Product> results = productRepository.findAll(spec);
    assertThat(
        "Должно быть две записи с 'Gadget' в названии или описании товара", results, hasSize(2));
  }

  @Test
  void testFindByPriceBetween() {
    Specification<Product> spec =
        ProductSpecifications.priceBetween(new BigDecimal("100"), new BigDecimal("500"));
    List<Product> results = productRepository.findAll(spec);
    assertThat("Должно быть 8 записей с ценой в диапазоне [100, 500].", results, hasSize(8));
  }

  @Test
  void testFindByNameStartsWith() {
    Specification<Product> spec = ProductSpecifications.nameStartsWith("Eco");
    List<Product> results = productRepository.findAll(spec);
    assertThat(
        "Должно быть три записи, название которых начинается на 'Eco'.", results, hasSize(3));
  }
}
