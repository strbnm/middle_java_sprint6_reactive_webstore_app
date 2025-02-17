package ru.strbnm.store.repository;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.strbnm.store.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findProductsByNameOrDescriptionContainingIgnoreCase(
      String name, String description, Pageable pageable);

  Page<Product> findProductByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

  Page<Product> findProductsByNameStartsWithIgnoreCase(String letter, Pageable pageable);
}
