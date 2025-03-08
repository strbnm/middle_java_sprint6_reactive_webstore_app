package ru.strbnm.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.ProductMapper;
import ru.strbnm.store.repository.ProductRepository;
import ru.strbnm.store.utils.ProductSortEnum;

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
    when(productRepository.findFilteredProducts(
            "Test",
            BigDecimal.ZERO,
            BigDecimal.valueOf(1000),
            "T",
            0,
            5,
            ProductSortEnum.NAME_ASC.getSortExpression()))
        .thenReturn(Flux.just(productA, productB, productC));

    StepVerifier.create(
            productService.getFilteredProducts(
                "Test",
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                "T",
                0,
                5,
                ProductSortEnum.NAME_ASC.getSortExpression()))
        .expectNext(productDtoA)
        .expectNext(productDtoB)
        .expectNext(productDtoC)
        .verifyComplete();

    verify(productRepository, times(1))
        .findFilteredProducts(
            "Test",
            BigDecimal.ZERO,
            BigDecimal.valueOf(1000),
            "T",
            0,
            5,
            ProductSortEnum.NAME_ASC.getSortExpression());
  }

  @Test
  void testGetProductById() {
    when(productRepository.findById(1L)).thenReturn(Mono.just(productA));

    StepVerifier.create(productService.getProductById(1L)).expectNext(productDtoA).verifyComplete();

    verify(productRepository, times(1)).findById(1L);
  }

  @Test
  void testGetCountAllProducts() {
    when(productRepository.count()).thenReturn(Mono.just(15L));

    StepVerifier.create(productService.getCountAllProducts()).expectNext(15L).verifyComplete();

    verify(productRepository, times(1)).count();
  }

  @TestConfiguration
  static class MapperTestConfig {
    @Bean
    public ProductMapper productMapper() {
      return Mappers.getMapper(ProductMapper.class);
    }
  }
}
