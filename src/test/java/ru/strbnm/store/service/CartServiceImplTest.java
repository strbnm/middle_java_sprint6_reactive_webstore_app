package ru.strbnm.store.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;
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

    cartItemA = CartItem.builder().id(1L).product(productA).quantity(2).build();

    cartItemB = CartItem.builder().id(2L).product(productB).quantity(10).build();

    cartItemDtoA = new CartItemDto(1L, 1L, 2);
    cartItemDtoB = new CartItemDto(2L, 2L, 10);
  }

  @Test
  void testGetCartItemsDto() {
    when(cartItemRepository.findAll()).thenReturn(List.of(cartItemA, cartItemB));

    List<CartItemDto> result = cartService.getCartItemsDto();
    assertThat("В списке позиций корзины две записи", result, hasSize(2));
    assertEquals(List.of(cartItemDtoA, cartItemDtoB), result);
  }

  @Test
  void testAddToCart_NewItem() {
    when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
    when(cartItemRepository.findByProduct(productA)).thenReturn(Optional.empty());
    when(cartItemRepository.save(any(CartItem.class))).thenReturn(cartItemA);

    CartItemDto result = cartService.addToCart(1L, 2);

    assertNotNull(result, "Объект не должен быть null.Объект не должен быть null.");
    assertEquals(cartItemDtoA, result);

    verify(productRepository, times(1)).findById(1L);
    verify(cartItemRepository, times(1)).findByProduct(productA);
    verify(cartItemRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  void testUpdateCartItemWithPositiveQuantity() {
    when(cartItemRepository.findById(2L)).thenReturn(Optional.of(cartItemB));
    when(cartItemRepository.save(any(CartItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    CartItemDto result = cartService.updateCartItem(2L, 2L, 15);
    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(
        15, result.getQuantity(), "Кол-во для позиции корзины с id=2 должно быть равно 15.");

    verify(cartItemRepository, times(1)).findById(2L);
    verify(cartItemRepository, times(1)).save(any(CartItem.class));
  }

  @Test
  void testUpdateCartItemWithZeroQuantity() {
    when(cartItemRepository.findById(1L)).thenReturn(Optional.of(cartItemA));
    doNothing().when(cartItemRepository).delete(cartItemA);

    CartItemDto result = cartService.updateCartItem(1L, 1L, 0);
    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(0, result.getQuantity(), "Кол-во для позиции корзины с id=1 должно быть равно 0.");

    verify(cartItemRepository, times(1)).findById(1L);
    verify(cartItemRepository, times(1)).delete(cartItemA);
  }

  @Test
  void testRemoveFromCart() {
    doNothing().when(cartItemRepository).deleteById(1L);

    assertDoesNotThrow(() -> cartService.removeFromCart(1L));
    verify(cartItemRepository, times(1)).deleteById(1L);
  }

  @Test
  void testGetCartSummary() {
    when(cartItemRepository.findAll()).thenReturn(List.of(cartItemA, cartItemB));

    CartDto result = cartService.getCartSummary();
    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(
        BigDecimal.valueOf(2200),
        result.getCartAmount(),
        "Общая стоимость товаров в корзине должна быть равна 2200.");
    assertEquals(List.of(cartItemA, cartItemB), result.getCartItems());
  }

  @Test
  void testGetCartInfo() {
    when(cartItemRepository.findAll()).thenReturn(List.of(cartItemA, cartItemB));

    CartInfoDto result = cartService.getCartInfo();
    assertNotNull(result, "Объект не должен быть null.");
    assertEquals(
        12, result.getCartItemsCount(), "Общее количество товаров в корзине должно быть равно 12.");
    assertEquals(
        BigDecimal.valueOf(2200),
        result.getCartAmount(),
        "Общая стоимость товаров в корзине должна быть равна 2200.");
  }

  @TestConfiguration
  static class MapperTestConfig {
    @Bean
    public CartItemMapper cartItemMapper() {
      return Mappers.getMapper(CartItemMapper.class);
    }
  }
}
