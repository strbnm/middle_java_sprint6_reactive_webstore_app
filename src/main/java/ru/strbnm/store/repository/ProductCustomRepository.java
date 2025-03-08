package ru.strbnm.store.repository;

import java.math.BigDecimal;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import ru.strbnm.store.entity.Product;

public interface ProductCustomRepository {
    Flux<Product> findFilteredProducts(
            String searchText,
            BigDecimal priceFrom,
            BigDecimal priceTo,
            String letter,
            int page,
            int size,
            Sort sorting);
}
