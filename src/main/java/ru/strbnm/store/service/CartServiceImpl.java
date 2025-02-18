package ru.strbnm.store.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.strbnm.store.dto.CartDTO;
import ru.strbnm.store.dto.CartItemDTO;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.CartItemMapper;
import ru.strbnm.store.repository.CartItemRepository;
import ru.strbnm.store.repository.ProductRepository;

@Service
public class CartServiceImpl implements CartService {
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final CartItemMapper cartItemMapper;

  @Autowired
  public CartServiceImpl(
      CartItemRepository cartItemRepository,
      ProductRepository productRepository,
      CartItemMapper cartItemMapper) {
    this.cartItemRepository = cartItemRepository;
    this.productRepository = productRepository;
    this.cartItemMapper = cartItemMapper;
  }

  @Override
  public List<CartItemDTO> getCartItems() {
    return cartItemMapper.toDTOs(cartItemRepository.findAll());
  }

  @Transactional
  @Override
  public void addToCart(Long productId, int quantity) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Товар не найден."));
    CartItem cartItem = CartItem.builder().product(product).quantity(quantity).build();
    cartItemRepository.save(cartItem);
  }

  @Transactional
  @Override
  public void updateCartItemQuantity(Long productId, int quantity) {
    CartItem cartItem =
        cartItemRepository.findAll().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Позиция корзины не найдена."));
    cartItem.setQuantity(quantity);
    cartItemRepository.save(cartItem);
  }

  @Transactional
  @Override
  public void removeFromCart(Long productId) {
    cartItemRepository.findAll().stream()
        .filter(item -> item.getProduct().getId().equals(productId))
        .findFirst()
        .ifPresent(cartItemRepository::delete);
  }

  @Override
  public CartDTO getCartSummary() {
    List<CartItemDTO> cartItemDTOList = getCartItems();
    BigDecimal cartAmount =
        cartItemDTOList.stream()
            .map(item -> item.getProductPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    return new CartDTO(cartItemDTOList, cartAmount);
  }
}
