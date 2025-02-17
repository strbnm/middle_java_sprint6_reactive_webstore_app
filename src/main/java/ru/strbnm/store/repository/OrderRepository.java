package ru.strbnm.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.strbnm.store.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {}
