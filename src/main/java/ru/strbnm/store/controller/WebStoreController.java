package ru.strbnm.store.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.service.CartService;
import ru.strbnm.store.service.OrderService;
import ru.strbnm.store.service.ProductService;
import ru.strbnm.store.utils.ProductSortEnum;

@Slf4j
@Controller
public class WebStoreController {

  private final int PAGINATION_SIZE = 10;
  private final ProductService productService;
  private final CartService cartService;
  private final OrderService orderService;

  @Autowired
  public WebStoreController(
      ProductService productService, CartService cartService, OrderService orderService) {
    this.productService = productService;
    this.cartService = cartService;
    this.orderService = orderService;
  }

  @GetMapping("/products")
  public String showProductShowcase(
      @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(value = "size", defaultValue = "" + PAGINATION_SIZE) @Min(10) @Max(100)
          int size,
      @RequestParam(value = "text", required = false) String searchText,
      @RequestParam(value = "price_from", required = false) BigDecimal priceFrom,
      @RequestParam(value = "price_to", required = false) BigDecimal priceTo,
      @RequestParam(value = "letter", required = false) String letter,
      @RequestParam(value = "sorting", defaultValue = "NAME_ASC") ProductSortEnum productSort,
      Model m) {
    Pageable pageRequest = PageRequest.of(page, size, productSort.getSortExpression());
    Page<ProductDto> products =
        productService.getFilteredProducts(searchText, priceFrom, priceTo, letter, pageRequest);

    // Преобразуем список корзины в Map
    Map<Long, CartItemDto> cartItemMap = cartService.getCartItemMap();

    // Вычисляем сумму количества товаров в корзине
    int cartTotalQuantity = cartItemMap.values().stream().mapToInt(CartItemDto::getQuantity).sum();
    m.addAttribute("products", products);
    m.addAttribute("size", size);
    m.addAttribute("sortOptions", ProductSortEnum.values());
    m.addAttribute("selectedSorting", productSort.name());
    m.addAttribute("cartItemMap", cartItemMap);
    m.addAttribute("totalQuantity", cartTotalQuantity);
    m.addAttribute("isShowCase", true);
    return "products/showcase";
  }

  @GetMapping("/products/{id}")
  public String getProductById(@PathVariable(value = "id") Long productId, Model m) {
    ProductDto product = productService.getProductById(productId);

    // Преобразуем список корзины в Map
    Map<Long, CartItemDto> cartItemMap = cartService.getCartItemMap();

    // Вычисляем сумму количества товаров в корзине
    int cartTotalQuantity = cartItemMap.values().stream().mapToInt(CartItemDto::getQuantity).sum();

    // Получаем объект корзины, если товар был добавлен в корзину или null
    CartItemDto cartItem = cartItemMap.get(product.getId());
    m.addAttribute("product", product);
    m.addAttribute("cartItem", cartItem);
    m.addAttribute("totalQuantity", cartTotalQuantity);
    return "products/show";
  }
}


