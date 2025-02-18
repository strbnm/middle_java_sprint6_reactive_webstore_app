package ru.strbnm.store.repository.spec;

import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import ru.strbnm.store.entity.Product;

public class ProductSpecifications {

  public static Specification<Product> nameOrDescriptionContains(String text) {
    return (root, query, criteriaBuilder) ->
        text == null || text.isEmpty()
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.or(
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), "%" + text.toLowerCase() + "%"),
                criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")),
                    "%" + text.toLowerCase() + "%"));
  }

  public static Specification<Product> priceBetween(BigDecimal priceFrom, BigDecimal priceTo) {
    return (root, query, criteriaBuilder) -> {
      if (priceFrom != null && priceTo != null) {
        return criteriaBuilder.between(root.get("price"), priceFrom, priceTo);
      } else if (priceFrom != null) {
        return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), priceFrom);
      } else if (priceTo != null) {
        return criteriaBuilder.lessThanOrEqualTo(root.get("price"), priceTo);
      }
      return criteriaBuilder.conjunction();
    };
  }

  public static Specification<Product> nameStartsWith(String letter) {
    return (root, query, criteriaBuilder) ->
        letter == null || letter.isEmpty()
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.like(
                criteriaBuilder.lower(root.get("name")), letter.toLowerCase() + "%");
  }
}
