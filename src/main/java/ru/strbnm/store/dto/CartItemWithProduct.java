package ru.strbnm.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Product;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CartItemWithProduct {
  private CartItem cartItem;
  private Product product;
}
