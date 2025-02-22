package ru.strbnm.store.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Product;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
  Optional<CartItem> findByProduct(Product product);
}
