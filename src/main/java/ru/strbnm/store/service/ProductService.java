package ru.strbnm.store.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.strbnm.store.dto.ProductDto;

public interface ProductService {
  Page<ProductDto> getFilteredProducts(
      String searchText,
      BigDecimal priceFrom,
      BigDecimal priceTo,
      String letter,
      Pageable pageable);

  ProductDto getProductById(Long productId);
}
