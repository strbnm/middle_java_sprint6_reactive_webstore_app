package ru.strbnm.store.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.strbnm.store.dto.ProductDTO;
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
      Model model) {
    Pageable pageRequest = PageRequest.of(page, size, productSort.getSortExpression());
    Page<ProductDTO> products =
        productService.getFilteredProducts(searchText, priceFrom, priceTo, letter, pageRequest);
    model.addAttribute("products", products);
    model.addAttribute("size", size);
    model.addAttribute("sortOptions", ProductSortEnum.values());
    model.addAttribute("selectedSorting", productSort.name());
    return "products/showcase";
  }
}
