package ru.strbnm.store.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.repository.CartItemRepository;

public class CartControllerIntegrationTest extends BaseControllersIntegrationTest {

  @Autowired private WebApplicationContext webApplicationContext;

  @Autowired private CartItemRepository cartItemRepository;

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  private CartItemDto cartItemDtoA, cartItemDtoB;

  /*
   После выполнения скрипта INIT_STORE_RECORD.sql таблица cart-items содержит следующие записи:
   id  product_id  quantity
   ========================
   1   2           10
   2   4           20
   3   6           30
   4   9           1
  */

  @BeforeEach
  void setUp() {
    cartItemDtoA = CartItemDto.builder().productId(1L).quantity(5).build();
    cartItemDtoB = CartItemDto.builder().id(1L).productId(2L).quantity(11).build();
  }

  @Transactional
  @Test
  void addToCart_shouldAddProductToCartItemInDatabaseAndReturnCartItemInfoDto() throws Exception {
    List<CartItem> cartItemsBeforeAdd = cartItemRepository.findAll();
    assertThat("Позиций в корзине до добавления должно быть 4.", cartItemsBeforeAdd, hasSize(4));
    List<String> oldProductNames =
        cartItemsBeforeAdd.stream().map(cartItem -> cartItem.getProduct().getName()).toList();

    mockMvc
        .perform(
            post("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemDtoA)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.cartItem.id").value(5L))
        .andExpect(jsonPath("$.cartItem.productId").value(cartItemDtoA.getProductId()))
        .andExpect(jsonPath("$.cartItem.quantity").value(cartItemDtoA.getQuantity()))
        .andExpect(jsonPath("$.cartItemsCount").value(66));
    ;

    List<CartItem> cartItemsAfterAdd = cartItemRepository.findAll();
    assertThat("Позиций в корзине после добавления должно быть 5.", cartItemsAfterAdd, hasSize(5));
    List<String> newProductNames =
        cartItemsAfterAdd.stream().map(cartItem -> cartItem.getProduct().getName()).toList();
    assertThat(
        newProductNames,
        allOf(hasItems(oldProductNames.toArray(new String[0])), hasItem("Eco Gadget 690")));
  }

  @Transactional
  @Test
  void UpdateCartItem_shouldUpdateCartItemInDatabaseAndReturnCartItemInfoDto() throws Exception {
    List<CartItem> cartItemsBeforeUpdate = cartItemRepository.findAll();
    assertThat("Позиций в корзине до обновления должно быть 4.", cartItemsBeforeUpdate, hasSize(4));
    List<String> oldProductNames =
        cartItemsBeforeUpdate.stream().map(cartItem -> cartItem.getProduct().getName()).toList();

    mockMvc
        .perform(
            put("/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cartItemDtoB)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.cartItem.id").value(cartItemDtoB.getId()))
        .andExpect(jsonPath("$.cartItem.productId").value(cartItemDtoB.getProductId()))
        .andExpect(jsonPath("$.cartItem.quantity").value(cartItemDtoB.getQuantity()))
        .andExpect(jsonPath("$.cartItemsCount").value(62));
    ;

    List<CartItem> cartItemsAfterAdd = cartItemRepository.findAll();
    assertThat("Позиций в корзине после обновления должно быть 4.", cartItemsAfterAdd, hasSize(4));
    List<String> newProductNames =
        cartItemsAfterAdd.stream().map(cartItem -> cartItem.getProduct().getName()).toList();
    assertThat(newProductNames, hasItems(oldProductNames.toArray(new String[0])));
  }
}
