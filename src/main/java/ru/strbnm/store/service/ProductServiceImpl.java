package ru.strbnm.store.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.mapper.ProductMapper;
import ru.strbnm.store.repository.ProductRepository;

@Service
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Autowired
  public ProductServiceImpl(
      ProductRepository productRepository, ProductMapper productMapper) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  public Flux<ProductDto> getFilteredProducts(
      String searchText,
      BigDecimal priceFrom,
      BigDecimal priceTo,
      String letter,
      int page,
      int size,
      Sort sorting) {

    return productRepository
        .findFilteredProducts(searchText, priceFrom, priceTo, letter, page, size, sorting)
        .map(productMapper::toDTO);
  }

  public Mono<Long> getCountAllProducts(){
    return productRepository.count();
  }

  @Override
  public Mono<ProductDto> getProductById(Long productId) {
    return productRepository.findById(productId).map(productMapper::toDTO);
  }
}
