package ru.strbnm.store.service;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.strbnm.store.dto.ProductDTO;
import ru.strbnm.store.entity.Product;

public interface ProductService {
  Page<ProductDTO> getFilteredProducts(
      String searchText,
      BigDecimal priceFrom,
      BigDecimal priceTo,
      String letter,
      Pageable pageable);

  ProductDTO getProductById(Long productId);

  Product getOne(Long productId);

  ProductDTO saveProduct(Product product);

  void deleteProduct(Long productId);
}
