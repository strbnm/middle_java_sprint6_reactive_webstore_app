package ru.strbnm.store.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Product;

public class CartItemRepositoryTest extends BaseRepositoryTest {
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

        List<CartItem> cartItems = cartItemRepository.findAll();

        HashMap<Long, CartItemRecord> actualCartItems = new HashMap<>();
        for (CartItem cartItem : cartItems) {
            actualCartItems.put(cartItem.getId(), new CartItemRecord(cartItem.getProduct().getId(), cartItem.getQuantity()));
        }

        assertThat("Должно быть 4 позиции в корзине.", cartItems, hasSize(4));
        assertEquals(expectedCartItems, actualCartItems, "id, product_id и quantity должны совпадать.");
    }

    @Test
    void save_shouldAddCartItemToDatabase() {
        Product product = productRepository.findById(5L).orElseThrow(() -> new RuntimeException("Товар не найден"));
        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(15)
                .build();

        cartItemRepository.save(cartItem);

        CartItem savedCartItem =
                cartItemRepository.findAll().stream()
                        .filter(createdCartItem -> createdCartItem.getId().equals(5L))
                        .findFirst()
                        .orElse(null);

        assertNotNull(savedCartItem, "Полученный из БД объект не должен быть null.");
        assertEquals(
                "Description for product_5",
                savedCartItem.getProduct().getDescription(),
                "Описание товара должно быть 'Description for product_5'.");
    assertEquals(15, savedCartItem.getQuantity(), "Количество товара быть равно 15.");
    }

    @Test
    void deleteById_shouldRemoveCartItemWithSpecifiedIdFromDatabase() {
        cartItemRepository.deleteById(3L);

        List<CartItem> cartItems = cartItemRepository.findAll();
        List<CartItem> deletedCartItem = cartItems.stream().filter(cartItem -> cartItem.getId().equals(3L)).toList();

        assertTrue(deletedCartItem.isEmpty(), "Должен быть пустой список результатов для позиции корзины с id=3.");
    }

    @Test
    void findByProduct_shouldReturnCartItemWhereProductEqualsOrNull() {
        Product product = productRepository.findById(9L).orElseThrow(() -> new RuntimeException("Товар не найден"));
        CartItem cartItem = cartItemRepository.findByProduct(product).orElse(null);

        assertNotNull(cartItem, "Полученный из БД объект не должен быть null.");
        assertEquals(product, cartItem.getProduct(), "Объекты типа Product должны быть равны.");
    }
}
