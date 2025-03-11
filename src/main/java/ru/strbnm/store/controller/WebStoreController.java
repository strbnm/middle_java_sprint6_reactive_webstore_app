package ru.strbnm.store.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring6.context.webflux.ReactiveDataDriverContextVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.service.CartService;
import ru.strbnm.store.service.ProductService;
import ru.strbnm.store.utils.ProductSortEnum;

@Controller
public class WebStoreController {

  private final int PAGINATION_SIZE = 10;
  private final ProductService productService;
  private final CartService cartService;

  @Autowired
  public WebStoreController(ProductService productService, CartService cartService) {
    this.productService = productService;
    this.cartService = cartService;
  }

  @GetMapping("/")
  public String redirect() {
    return "redirect:products";
  }

  @GetMapping("/products")
  public Mono<String> showProductShowcase(
      @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(value = "size", defaultValue = "" + PAGINATION_SIZE) @Min(5) @Max(100) int size,
      @RequestParam(value = "text", required = false) String searchText,
      @RequestParam(value = "price_from", required = false) BigDecimal priceFrom,
      @RequestParam(value = "price_to", required = false) BigDecimal priceTo,
      @RequestParam(value = "letter", required = false) String letter,
      @RequestParam(value = "sorting", defaultValue = "NAME_ASC") ProductSortEnum productSort,
      Model m) {

    Flux<ProductDto> productsFlux =
        productService.getFilteredProducts(
            searchText, priceFrom, priceTo, letter, page, size, productSort.getSortExpression());

    Mono<Map<Long, CartItemDto>> cartItemsMono = cartService.getCartItemMap();
    Mono<Long> totalElementsMono = productService.getCountAllProducts();

    return Mono.zip(cartItemsMono, totalElementsMono)
        .doOnNext(
            tuple -> {
              Map<Long, CartItemDto> cartItemMap = tuple.getT1();
              Long totalElements = tuple.getT2();
              int totalPages = (int) Math.ceil((double) totalElements / size);
              int cartTotalQuantity =
                  cartItemMap.values().stream().mapToInt(CartItemDto::getQuantity).sum();

              m.addAttribute("size", size);
              m.addAttribute("sortOptions", ProductSortEnum.values());
              m.addAttribute("selectedSorting", productSort.name());
              m.addAttribute("cartItemMap", cartItemMap);
              m.addAttribute("cartTotalQuantity", cartTotalQuantity);
              m.addAttribute("isShowCase", true);
              m.addAttribute("totalElements", totalElements);
              m.addAttribute("pageNumber", page);
              m.addAttribute("totalPages", totalPages);
            })
        .thenReturn("products/showcase")
        .doOnSuccess(
            viewName ->
                m.addAttribute(
                    "products",
                    new ReactiveDataDriverContextVariable(
                        productsFlux,
                        size)));
  }

  @GetMapping("/products/{id}")
  public Mono<String> getProductById(@PathVariable Long id, Model model) {
    Mono<ProductDto> productMono = productService.getProductById(id);
    Mono<Map<Long, CartItemDto>> cartItemsMono = cartService.getCartItemMap();

    return Mono.zip(productMono, cartItemsMono)
        .doOnNext(
            tuple -> {
              ProductDto product = tuple.getT1();
              Map<Long, CartItemDto> cartItemMap = tuple.getT2();

              // Вычисляем общее количество товаров в корзине
              int cartTotalQuantity =
                  cartItemMap.values().stream().mapToInt(CartItemDto::getQuantity).sum();

              // Получаем объект корзины, если товар был добавлен
              CartItemDto cartItem = cartItemMap.get(product.getId());

              model.addAttribute("product", product);
              model.addAttribute("cartItem", cartItem);
              model.addAttribute("cartTotalQuantity", cartTotalQuantity);
            })
        .thenReturn("products/product_detail")
        .switchIfEmpty(
            Mono.defer(
                () -> {
                  model.addAttribute("error", "Not Found");
                  return Mono.just("errors/4xx");
                })); // если товар не найден
  }
}
