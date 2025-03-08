package ru.strbnm.store.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.CartDto;
import ru.strbnm.store.dto.CartInfoDto;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.dto.CartItemWithProduct;
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
  public Flux<CartItemDto> getCartItemsDto() {

    return cartItemRepository.findAll().map(cartItemMapper::toDto);
  }

  @Transactional
  @Override
  public Mono<CartItemDto> addToCart(Long productId, int quantity) {
    return productRepository
        .findById(productId)
        .switchIfEmpty(Mono.error(new RuntimeException("Товар не найден.")))
        .flatMap(
            product ->
                cartItemRepository
                    .findByProductId(product.getId())
                    .defaultIfEmpty(
                        CartItem.builder().productId(product.getId()).quantity(0).build())
                    .flatMap(
                        cartItem -> {
                          cartItem.setQuantity(cartItem.getQuantity() + quantity);
                          return cartItemRepository.save(cartItem);
                        }))
        .map(cartItemMapper::toDto);
  }

  @Transactional
  @Override
  public Mono<CartItemDto> updateCartItem(Long id, Long productId, int quantity) {
    return cartItemRepository
        .findById(id)
        .switchIfEmpty(Mono.error(new RuntimeException("Элемент корзины не найден")))
        .flatMap(
            cartItem -> {
              if (quantity == 0) {
                return cartItemRepository
                    .delete(cartItem)
                    .thenReturn(
                        CartItemDto.builder()
                            .id(cartItem.getId())
                            .productId(productId)
                            .quantity(0)
                            .build());
              }
              cartItem.setQuantity(quantity);
              return cartItemRepository.save(cartItem).map(cartItemMapper::toDto);
            });
  }

  @Transactional
  @Override
  public Mono<Void> removeFromCart(Long cartItemId) {
    return cartItemRepository.deleteById(cartItemId);
  }

  @Transactional
  @Override
  public Mono<CartDto> getCartSummary() {
    return cartItemRepository
        .findAllWithProducts()
        .collect(CartSummary::new, CartSummary::accumulate)
        .map(
            summary ->
                new CartDto(
                    summary.getCartItemsWithProduct(),
                    summary.getTotalAmount(),
                    summary.getTotalQuantity()));
  }

  @Transactional
  @Override
  public Mono<CartInfoDto> getCartInfo() {
    return cartItemRepository
        .findAllWithProducts()
        .collect(CartSummary::new, CartSummary::accumulate)
        .map(summary -> new CartInfoDto(summary.getTotalQuantity(), summary.getTotalAmount()));
  }

  @Override
  public Mono<Map<Long, CartItemDto>> getCartItemMap() {
    return cartItemRepository
        .findAllWithProducts()
        .map(itemWithProduct -> cartItemMapper.toDto(itemWithProduct.getCartItem()))
        .collectMap(CartItemDto::getProductId, item -> item);
  }

  class CartSummary {
    private final List<CartItemDto> items = new ArrayList<>();
    private final List<CartItemWithProduct> cartItemsWithProduct = new ArrayList<>();
    private BigDecimal totalAmount = BigDecimal.ZERO;
    private int totalQuantity = 0;

    void accumulate(CartItemWithProduct itemWithProduct) {
      CartItem item = itemWithProduct.getCartItem();
      Product product = itemWithProduct.getProduct();

      cartItemsWithProduct.add(itemWithProduct);
      CartItemDto dto = new CartItemDto(item.getId(), item.getProductId(), item.getQuantity());
      items.add(dto);
      totalAmount =
          totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
      totalQuantity += item.getQuantity();
    }

    List<CartItemDto> getItems() {
      return items;
    }

    List<CartItemWithProduct> getCartItemsWithProduct() {
      return cartItemsWithProduct;
    }

    BigDecimal getTotalAmount() {
      return totalAmount;
    }

    int getTotalQuantity() {
      return totalQuantity;
    }
  }
}
