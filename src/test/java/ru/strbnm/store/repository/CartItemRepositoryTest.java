package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import ru.strbnm.store.entity.CartItem;

public class CartItemRepositoryTest extends BaseR2dbcTest {
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private ProductRepository productRepository;

  /*
   После выполнения скрипта INIT_STORE_RECORD.sql таблица cart-items содержит следующие записи:
   id  product_id  quantity
   ========================
   1   2           10
   2   4           20
   3   6           30
   4   9           1
  */

  private record CartItemRecord(Long product_id, int quantity) {}

    @Test
    void findAll_shouldReturnAllCartItems() {
        HashMap<Long, CartItemRecord> expectedCartItems = new HashMap<>();
        expectedCartItems.put(1L, new CartItemRecord(2L, 10));
        expectedCartItems.put(2L, new CartItemRecord(4L, 20));
        expectedCartItems.put(3L, new CartItemRecord(6L, 30));
        expectedCartItems.put(4L, new CartItemRecord(9L, 1));

        cartItemRepository.findAll()
                .collectList()
                .as(StepVerifier::create)
                .assertNext(cartItems -> {
                    HashMap<Long, CartItemRecord> actualCartItems = new HashMap<>();
                    for (CartItem cartItem : cartItems) {
                        actualCartItems.put(cartItem.getId(), new CartItemRecord(cartItem.getProductId(), cartItem.getQuantity()));
                    }
                    assertThat("Должно быть 4 позиции в корзине.", cartItems, hasSize(4));
                    assertEquals(expectedCartItems, actualCartItems, "id, product_id и quantity должны совпадать.");
                })
                .verifyComplete();
    }


    @Test
    void findById_shouldReturnCartItemsWithSpecificId() {
        cartItemRepository.findById(2L)
                .as(StepVerifier::create)
                .assertNext(cartItem -> {
                    assertEquals(4L, cartItem.getProductId(), "id товара должно быть 4.");
                    assertEquals(20, cartItem.getQuantity(), "Количество товара должно быть 20.");
                })
                .verifyComplete();
    }


    @Test
    void save_shouldAddCartItemToDatabase() {
        productRepository.findById(5L)
                .flatMap(product -> {
                    CartItem cartItem = CartItem.builder()
                            .productId(product.getId())
                            .quantity(15)
                            .build();
                    return cartItemRepository.save(cartItem);
                })
                .flatMap(savedCartItem -> cartItemRepository.findById(savedCartItem.getId()))
                .as(StepVerifier::create)
                .assertNext(savedCartItem -> {
                    assertNotNull(savedCartItem, "Полученный из БД объект не должен быть null.");
                    assertEquals(5L, savedCartItem.getProductId(), "ID товара должен быть 5.");
                    assertEquals(15, savedCartItem.getQuantity(), "Количество товара должно быть равно 15.");
                })
                .verifyComplete();
    }

    @Test
    void deleteById_shouldRemoveCartItemWithSpecifiedIdFromDatabase() {
        cartItemRepository.deleteById(3L) // Удаляем элемент
                .then(cartItemRepository.findAll().collectList()) // Получаем все элементы после удаления
                .as(StepVerifier::create)
                .assertNext(cartItems -> {
                    boolean isDeleted = cartItems.stream().noneMatch(cartItem -> cartItem.getId().equals(3L));
                    assertTrue(isDeleted, "Должен быть пустой список результатов для позиции корзины с id=3.");
                })
                .verifyComplete();
    }

    @Test
    void findByProduct_shouldReturnCartItemWhereProductEqualsOrNull() {
      Long expectedProductId = 9L;
        productRepository.findById(expectedProductId)
                .flatMap(product -> cartItemRepository.findByProductId(product.getId()))
                .as(StepVerifier::create)
                .assertNext(cartItem -> {
                    assertNotNull(cartItem, "Полученный из БД объект не должен быть null.");
                    assertEquals(expectedProductId, cartItem.getProductId(), "Id товара должен быть равен 9.");
                })
                .verifyComplete();
    }
}
