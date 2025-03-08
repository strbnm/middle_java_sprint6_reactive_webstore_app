package ru.strbnm.store.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.ProductDto;

public interface ProductService {
  Flux<ProductDto> getFilteredProducts(
      String searchText,
      BigDecimal priceFrom,
      BigDecimal priceTo,
      String letter,
      int page,
      int size,
      Sort sorting);

  Mono<ProductDto> getProductById(Long productId);
  Mono<Long> getCountAllProducts();
}
