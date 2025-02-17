package ru.strbnm.store.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

@Value
public class ProductDTO {
  Long id;

  @NotBlank(message = "Наименование товара не должно быть пустым.")
  @Size(min = 2, message = "Наименование товара должно состоять из не менее 2 символов.")
  String name;

  @NotBlank(message = "Описание товара не может быть пустым.")
  String description;
  String imageUrl;

  @Positive(message = "Цена товара должна быть положительным числом.")
  BigDecimal price;
}
