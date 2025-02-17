package ru.strbnm.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.strbnm.store.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {}
