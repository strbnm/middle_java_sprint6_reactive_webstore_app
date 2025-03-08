package ru.strbnm.store.repository;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.dto.OrderItemDto;

@Repository
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

  private final DatabaseClient databaseClient;

  @Autowired
  public OrderCustomRepositoryImpl(@NonNull R2dbcEntityTemplate template) {
    this.databaseClient = template.getDatabaseClient();
  }

  private record OrderItemWithOrder(Long orderId, BigDecimal totalPrice, OrderItemDto orderItem) {}

  @Override
  public Mono<OrderDto> findOrderWithItemsAndProducts(Long orderId) {
    String query =
        """
        SELECT o.id as order_id, o.total_price,
               oi.id as item_id, oi.product_id, oi.quantity, oi.price,
               p.name as product_name, p.image_url as product_image_url
        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        LEFT JOIN products p ON oi.product_id = p.id
        WHERE o.id = :orderId
        """;

    return databaseClient
        .sql(query)
        .bind("orderId", orderId)
        .map(
            (row, metadata) ->
                new OrderItemDto(
                    row.get("product_id", Long.class),
                    row.get("product_name", String.class),
                    row.get("product_image_url", String.class),
                    Optional.ofNullable(row.get("quantity", Integer.class)).orElse(0),
                    row.get("price", BigDecimal.class)))
        .all()
        .collectList()
        .flatMap(
            items ->
                items.isEmpty()
                    ? Mono.empty()
                    : Mono.just(
                        new OrderDto(
                            orderId,
                            items.stream()
                                .map(OrderItemDto::getPrice)
                                .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add), // Суммируем стоимость товаров
                            items)));
  }

  @Override
  public Flux<OrderDto> findAllOrdersWithItemsAndProducts() {
    String query =
        """
        SELECT o.id as order_id, o.total_price,
               oi.id as item_id, oi.product_id, oi.quantity, oi.price,
               p.name as product_name, p.image_url as product_image_url
        FROM orders o
        LEFT JOIN order_items oi ON o.id = oi.order_id
        LEFT JOIN products p ON oi.product_id = p.id
        """;

    return databaseClient
        .sql(query)
        .map(
            (row, metadata) ->
                new OrderItemWithOrder(
                    row.get("order_id", Long.class),
                    row.get("total_price", BigDecimal.class),
                    new OrderItemDto(
                        row.get("product_id", Long.class),
                        row.get("product_name", String.class),
                        row.get("product_image_url", String.class),
                        Optional.ofNullable(row.get("quantity", Integer.class)).orElse(0),
                        row.get("price", BigDecimal.class))))
        .all()
        .collectMultimap(OrderItemWithOrder::orderId) // Группируем по orderId
        .flatMapMany(map -> Flux.fromIterable(map.entrySet()))
        .map(
            entry ->
                new OrderDto(
                    entry.getKey(), // orderId
                    entry.getValue().stream()
                        .map(OrderItemWithOrder::totalPrice)
                        .findFirst()
                        .orElse(BigDecimal.ZERO),
                    entry.getValue().stream().map(OrderItemWithOrder::orderItem).toList()));
  }
}
