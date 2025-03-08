package ru.strbnm.store.repository;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.strbnm.store.dto.CartItemWithProduct;
import ru.strbnm.store.entity.CartItem;
import ru.strbnm.store.entity.Product;

@Repository
public class CartItemCustomRepositoryImpl implements CartItemCustomRepository {
  private final DatabaseClient databaseClient;

  @Autowired
  public CartItemCustomRepositoryImpl(@NonNull R2dbcEntityTemplate template) {
    this.databaseClient = template.getDatabaseClient();
  }

  @Override
  public Flux<CartItemWithProduct> findAllWithProducts() {
    String query =
        """
            SELECT ci.id as cart_item_id, ci.product_id, ci.quantity,
                   p.id as product_id, p.name, p.description, p.price, p.image_url
            FROM cart_items ci
            JOIN products p ON ci.product_id = p.id
            """;

    return databaseClient
        .sql(query)
        .map(
            row ->
                new CartItemWithProduct(
                    CartItem.builder()
                            .id(row.get("cart_item_id", Long.class))
                            .productId(row.get("product_id", Long.class))
                            .quantity(Optional.ofNullable(row.get("quantity", Integer.class)).orElse(0))
                            .build(),
                    Product.builder()
                            .id(row.get("product_id", Long.class))
                            .name(row.get("name", String.class))
                            .description(row.get("description", String.class))
                            .imageUrl(row.get("image_url", String.class))
                            .price(row.get("price", BigDecimal.class))
                            .build()
                        ))
        .all();
  }
}
