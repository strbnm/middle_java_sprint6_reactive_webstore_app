package ru.strbnm.store.repository;

import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.strbnm.store.entity.Product;

@Repository
@RequiredArgsConstructor
public class ProductCustomRepositoryImpl implements ProductCustomRepository{
  private final R2dbcEntityTemplate template;

  public Flux<Product> findFilteredProducts(
      String searchText,
      BigDecimal priceFrom,
      BigDecimal priceTo,
      String letter,
      int page,
      int size,
      Sort sorting) {
    Criteria criteria = Criteria.empty().ignoreCase(true);
    if (searchText != null && !searchText.isEmpty()) {
      String searchPattern = "%" + searchText + "%";
      Criteria searchCriteria = Criteria
              .where("name")
              .like(searchPattern)
              .or("description")
              .like(searchPattern);

      criteria = criteria.and(searchCriteria); // Группируем условия
    }
    if (priceFrom != null) {
      criteria = criteria.and("price").greaterThanOrEquals(priceFrom);
    }
    if (priceTo != null) {
      criteria = criteria.and("price").lessThanOrEquals(priceTo);
    }
    if (letter != null && !letter.isEmpty()) {
      criteria = criteria.and("name").like(letter + "%");
    }

    Query query = Query.query(criteria).sort(sorting).limit(size).offset((long) page * size);

    return template.select(query, Product.class);
  }

}
