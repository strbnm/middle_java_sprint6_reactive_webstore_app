package ru.strbnm.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.strbnm.store.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {}
