package ru.strbnm.store.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;
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
  public List<CartItemDto> getCartItemsDto() {
    return cartItemMapper.toDTOs(cartItemRepository.findAll());
  }

  @Transactional
  @Override
  public CartItemDto addToCart(Long productId, int quantity) {
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Товар не найден."));

    Optional<CartItem> existingItem = cartItemRepository.findByProduct(product);

    CartItem cartItem;
    if (existingItem.isPresent()) {
      cartItem = existingItem.get();
      cartItem.setQuantity(cartItem.getQuantity() + quantity);
    } else {
      cartItem = CartItem.builder().product(product).quantity(quantity).build();
    }

    CartItem savedItem = cartItemRepository.save(cartItem);
    return cartItemMapper.toDto(savedItem);
  }

  @Transactional
  @Override
  public CartItemDto updateCartItem(Long id, Long productId, int quantity) {
    CartItem cartItem =
        cartItemRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Элемент корзины не найден"));

    if (quantity == 0) {
      cartItemRepository.delete(cartItem);
      return CartItemDto.builder().id(cartItem.getId()).productId(productId).quantity(0).build();
    }

    cartItem.setQuantity(quantity);
    CartItem updatedItem = cartItemRepository.save(cartItem);
    return cartItemMapper.toDto(updatedItem);
  }

  @Transactional
  @Override
  public void removeFromCart(Long cartItemId) {
    cartItemRepository.deleteById(cartItemId);
  }

  @Transactional
  @Override
  public CartDto getCartSummary() {
    List<CartItem> cartItems = cartItemRepository.findAll();
    BigDecimal cartAmount = calculateCartAmount(cartItems);
    return new CartDto(cartItems, cartAmount);
  }

  @Transactional
  @Override
  public CartInfoDto getCartInfo() {
    List<CartItem> cartItems = cartItemRepository.findAll();
    BigDecimal cartAmount = calculateCartAmount(cartItems);
    Integer cartItemsCount = calculateCartItemsCount(cartItems);
    return new CartInfoDto(cartItemsCount, cartAmount);
  }

  @Override
  public Map<Long, CartItemDto> getCartItemMap() {
    List<CartItemDto> cartItems = getCartItemsDto();
    return cartItems.stream().collect(Collectors.toMap(CartItemDto::getProductId, item -> item));
  }

  private BigDecimal calculateCartAmount(List<CartItem> cartItems) {
    return cartItems.stream()
        .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private Integer calculateCartItemsCount(List<CartItem> cartItems) {
    return cartItems.stream().mapToInt(CartItem::getQuantity).sum();
  }
}
