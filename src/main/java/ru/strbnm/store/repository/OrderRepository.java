package ru.strbnm.store.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.strbnm.store.entity.Order;

@Repository
public interface OrderRepository
    extends ReactiveCrudRepository<Order, Long>, OrderCustomRepository {}
