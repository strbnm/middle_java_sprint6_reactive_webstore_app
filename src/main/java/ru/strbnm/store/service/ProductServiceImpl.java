package ru.strbnm.store.service;

import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.entity.Product;
import ru.strbnm.store.mapper.ProductMapper;
import ru.strbnm.store.repository.ProductRepository;
import ru.strbnm.store.repository.spec.ProductSpecifications;

@Service
public class ProductServiceImpl implements ProductService {
  private final ProductRepository productRepository;
  private final ProductMapper productMapper;

  @Autowired
  public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
    this.productRepository = productRepository;
    this.productMapper = productMapper;
  }

  public Page<ProductDto> getFilteredProducts(
      String searchText,
      BigDecimal priceFrom,
      BigDecimal priceTo,
      String letter,
      Pageable pageable) {

    Specification<Product> spec =
        Specification.where(ProductSpecifications.nameOrDescriptionContains(searchText))
            .and(ProductSpecifications.priceBetween(priceFrom, priceTo))
            .and(ProductSpecifications.nameStartsWith(letter));

    return productRepository.findAll(spec, pageable).map(productMapper::toDTO);
  }

  @Override
  public ProductDto getProductById(Long productId) {
    Product existsProduct =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Товар не найден"));
    return productMapper.toDTO(existsProduct);
  }
}
