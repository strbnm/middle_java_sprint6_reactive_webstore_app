package ru.strbnm.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.strbnm.store.entity.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {}
