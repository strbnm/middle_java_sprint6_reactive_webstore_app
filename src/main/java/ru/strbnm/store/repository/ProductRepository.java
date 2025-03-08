package ru.strbnm.store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.strbnm.store.entity.Product;

@Repository
public interface ProductRepository
        extends ReactiveCrudRepository<Product, Long>, ProductCustomRepository {}
