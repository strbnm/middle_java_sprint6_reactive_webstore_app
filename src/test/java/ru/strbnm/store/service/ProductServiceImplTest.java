package ru.strbnm.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.ProductMapper;
import ru.strbnm.store.repository.ProductRepository;

@SpringBootTest(classes = {ProductServiceImpl.class, ProductServiceImplTest.MapperTestConfig.class})
public class ProductServiceImplTest {
  @MockitoBean private ProductRepository productRepository;

  @Autowired private ProductMapper productMapper;

  @Autowired private ProductServiceImpl productService;

  private Product productA;
  private Product productB;
  private Product productC;

  private ProductDto productDtoA;
  private ProductDto productDtoB;
  private ProductDto productDtoC;

  @BeforeEach
  void setUp() {
    productA =
        Product.builder()
            .id(1L)
            .name("Test product A")
            .description("Test product A description")
            .imageUrl("test_product_a.png")
            .price(BigDecimal.valueOf(100))
            .build();
    productB =
        Product.builder()
            .id(2L)
            .name("Test product B")
            .description("Test product B description")
            .imageUrl("test_product_b.png")
            .price(BigDecimal.valueOf(200))
            .build();
    productC =
        Product.builder()
            .id(3L)
            .name("Test product C")
            .description("Test product C description")
            .imageUrl("test_product_c.png")
            .price(BigDecimal.valueOf(300))
            .build();

    productDtoA =
        new ProductDto(
            1L,
            "Test product A",
            "Test product A description",
            "test_product_a.png",
            BigDecimal.valueOf(100));
    productDtoB =
        new ProductDto(
            2L,
            "Test product B",
            "Test product B description",
            "test_product_b.png",
            BigDecimal.valueOf(200));
    productDtoC =
        new ProductDto(
            3L,
            "Test product C",
            "Test product C description",
            "test_product_c.png",
            BigDecimal.valueOf(300));
  }

  @Test
  void testGetFilteredProducts() {
    Pageable pageable = PageRequest.of(0, 10);
    Page<Product> productPage = new PageImpl<>(List.of(productA, productB, productC), pageable, 4);

    when(productRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(productPage);

    Page<ProductDto> result =
        productService.getFilteredProducts(
            "Test", BigDecimal.ZERO, BigDecimal.valueOf(1000), "T", pageable);

    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(3, result.getTotalElements(), "Объект Page должен содержать 3 элемента.");
    assertEquals(List.of(productDtoA, productDtoB, productDtoC), result.getContent());
  }

  @Test
  void testGetProductById_WhenProductExists() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(productA));

    ProductDto result = productService.getProductById(1L);

    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(1L, result.getId(), "Id товара должен быть равен 1.");
    assertEquals(
        "Test product A", result.getName(), "Название товара должно быть равно 'Test product A'.");
  }

  @Test
  void testGetProductById_WhenProductNotFound() {
    when(productRepository.findById(1L)).thenReturn(Optional.empty());

    RuntimeException exception =
        assertThrows(RuntimeException.class, () -> productService.getProductById(1L));
    assertEquals("Товар не найден", exception.getMessage());
  }

  @TestConfiguration
  static class MapperTestConfig {
    @Bean
    public ProductMapper productMapper() {
      return Mappers.getMapper(ProductMapper.class);
    }
  }
}
