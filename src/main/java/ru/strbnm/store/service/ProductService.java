package ru.strbnm.store.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.strbnm.store.dto.ProductDTO;
import ru.strbnm.store.entity.Product;

import java.math.BigDecimal;

public interface ProductService {
    Page<ProductDTO> findAllProductPagingAndSorting(Pageable pageable);
    Page<ProductDTO> findAllProductByNameOrDescriptionContaining(String search, Pageable pageable);
    Page<ProductDTO> findAllProductByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    Page<ProductDTO> findAllProductByNameStartsWith(String search, Pageable pageable);
    ProductDTO getProductById (Long productId);
    Product getOne(Long productId);
    ProductDTO saveProduct(Product product);
    void deleteProduct(Long productId);
}
