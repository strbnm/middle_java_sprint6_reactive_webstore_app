package ru.strbnm.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import java.math.BigDecimal;
import java.util.List;
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
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.dto.CartItemWithProduct;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.CartItemMapper;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.ProductRepository;

@SpringBootTest(classes = {CartServiceImpl.class, CartServiceImplTest.MapperTestConfig.class})
public class CartServiceImplTest {
  @MockitoBean private CartItemRepository cartItemRepository;

  @MockitoBean private ProductRepository productRepository;

  @Autowired private CartItemMapper cartItemMapper;
  @Autowired private CartServiceImpl cartService;

  private Product productA;
  private Product productB;
  private CartItem cartItemA;
  private CartItem cartItemB;
  private CartItemDto cartItemDtoA;
  private CartItemDto cartItemDtoB;
  private CartItemWithProduct cartItemWithProductA;
  private CartItemWithProduct cartItemWithProductB;

  @BeforeEach
  void setUp() {
    productA =
        Product.builder()
            .id(1L)
            .name("Test product #1")
            .description("Test product #1 description")
            .imageUrl("test_product_1.png")
            .price(BigDecimal.valueOf(100))
            .build();

    productB =
        Product.builder()
            .id(2L)
            .name("Test product #2")
            .description("Test product #2 description")
            .imageUrl("test_product_2.png")
            .price(BigDecimal.valueOf(200))
            .build();

    cartItemA = CartItem.builder().id(1L).productId(productA.getId()).quantity(2).build();

    cartItemB = CartItem.builder().id(2L).productId(productB.getId()).quantity(10).build();

    cartItemDtoA = new CartItemDto(1L, 1L, 2);
    cartItemDtoB = new CartItemDto(2L, 2L, 10);

    cartItemWithProductA = CartItemWithProduct.builder().cartItem(cartItemA).product(productA).build();
    cartItemWithProductB = CartItemWithProduct.builder().cartItem(cartItemB).product(productB).build();
  }

  @Test
  void testGetCartItemsDto() {
    when(cartItemRepository.findAll()).thenReturn(Flux.just(cartItemA, cartItemB));

    StepVerifier.create(cartService.getCartItemsDto())
            .expectNext(cartItemDtoA)
            .expectNext(cartItemDtoB)
            .verifyComplete();

    verify(cartItemRepository, times(1)).findAll();
  }

  @Test
  void testAddToCart_NewItem() {
    when(productRepository.findById(1L)).thenReturn(Mono.just(productA));
    when(cartItemRepository.findByProductId(productA.getId())).thenReturn(Mono.empty());
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItemA));

    StepVerifier.create(cartService.addToCart(1L, 2))
            .expectNext(cartItemDtoA)
            .verifyComplete();

    verify(productRepository, times(1)).findById(1L);
    verify(cartItemRepository, times(1)).findByProductId(productA.getId());
    verify(cartItemRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  void testUpdateCartItemWithPositiveQuantity() {
    when(cartItemRepository.findById(2L)).thenReturn(Mono.just(cartItemB));
    when(cartItemRepository.save(any(CartItem.class)))
        .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

    StepVerifier.create(cartService.updateCartItem(2L, 2L, 15))
                    .assertNext(updatedCartItem -> {
                      assertNotNull(updatedCartItem, "Объект не должен быть null.");
                      assertEquals(
                              15, updatedCartItem.getQuantity(), "Кол-во для позиции корзины с id=2 должно быть равно 15.");
                    })
                            .verifyComplete();

    verify(cartItemRepository, times(1)).findById(2L);
    verify(cartItemRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  void testUpdateCartItemWithZeroQuantity() {
    when(cartItemRepository.findById(1L)).thenReturn(Mono.just(cartItemA));
    when(cartItemRepository.delete(cartItemA)).thenReturn(Mono.empty());

    StepVerifier.create(cartService.updateCartItem(1L, 1L, 0))
                    .assertNext(removedCartItem -> {
                      assertNotNull(removedCartItem, "Объект не должен быть null.");
                      assertEquals(0, removedCartItem.getQuantity(), "Кол-во для позиции корзины с id=1 должно быть равно 0.");
                    })
                            .verifyComplete();

    verify(cartItemRepository, times(1)).findById(1L);
    verify(cartItemRepository, times(1)).delete(cartItemA);
  }

  @Test
  void testRemoveFromCart() {
    when(cartItemRepository.deleteById(1L)).thenReturn(Mono.empty());

    StepVerifier.create(cartService.removeFromCart(1L))
            .verifyComplete();
    verify(cartItemRepository, times(1)).deleteById(1L);
  }

  @Test
  void testGetCartSummary() {
    when(cartItemRepository.findAllWithProducts()).thenReturn(Flux.just(cartItemWithProductA, cartItemWithProductB));

    StepVerifier.create(cartService.getCartSummary())
                    .assertNext(cartDto -> {
                      assertNotNull(cartDto, "Объект не должен быть null.");
                      assertEquals(
                              BigDecimal.valueOf(2200),
                              cartDto.getCartAmount(),
                              "Общая стоимость товаров в корзине должна быть равна 2200.");
                      assertEquals(List.of(cartItemWithProductA, cartItemWithProductB), cartDto.getCartItemsWithProduct());
                    })
            .verifyComplete();
  }

  @Test
  void testGetCartInfo() {
    when(cartItemRepository.findAllWithProducts()).thenReturn(Flux.just(cartItemWithProductA, cartItemWithProductB));

    StepVerifier.create(cartService.getCartInfo())
                    .assertNext(cartInfoDto -> {
                      assertNotNull(cartInfoDto, "Объект не должен быть null.");
                      assertEquals(
                              12, cartInfoDto.getCartItemsCount(), "Общее количество товаров в корзине должно быть равно 12.");
                      assertEquals(
                              BigDecimal.valueOf(2200),
                              cartInfoDto.getCartAmount(),
                              "Общая стоимость товаров в корзине должна быть равна 2200.");
                    })
            .verifyComplete();
  }

  @TestConfiguration
  static class MapperTestConfig {
    @Bean
    public CartItemMapper cartItemMapper() {
      return Mappers.getMapper(CartItemMapper.class);
    }
  }
}
