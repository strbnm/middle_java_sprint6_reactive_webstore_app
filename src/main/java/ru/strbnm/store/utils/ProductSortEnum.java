package ru.strbnm.store.utils;

import org.springframework.data.domain.Sort;

public enum ProductSortEnum {
  PRICE_ASC("По возрастанию цены", Sort.by("price").ascending()),
  PRICE_DESC("По убыванию цены", Sort.by("price").descending()),
  NAME_ASC("По алфавиту по возрастанию", Sort.by("name").ascending()),
  NAME_DESC("По алфавиту по убыванию", Sort.by("name").descending());

  private final String description;
  private final Sort sortExpression;

  ProductSortEnum(String description, Sort sortExpression) {
    this.description = description;
    this.sortExpression = sortExpression;
  }

  public String getDescription() {
    return description;
  }

  public Sort getSortExpression() {
    return sortExpression;
  }
}
