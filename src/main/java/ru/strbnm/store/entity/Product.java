package ru.strbnm.store.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false)
  private Long id;

  @NonNull
  @NotBlank(message = "Наименование товара не должно быть пустым.")
  @Size(min = 2, message = "Наименование товара должно состоять из не менее 2 символов.")
  @Column(name = "name", nullable = false)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private String name;

  @NonNull
  @Lob
  @NotBlank(message = "Описание товара не может быть пустым.")
  @Column(name = "description", nullable = false)
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private String description;

  @Column(name = "image_url")
  @JdbcTypeCode(SqlTypes.VARCHAR)
  private String imageUrl;

  @Positive(message = "Цена товара должна быть положительным числом.")
  @Column(name = "price", nullable = false)
  @JdbcTypeCode(SqlTypes.NUMERIC)
  private BigDecimal price;

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    Class<?> oEffectiveClass =
        o instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : o.getClass();
    Class<?> thisEffectiveClass =
        this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
    if (thisEffectiveClass != oEffectiveClass) return false;
    Product product = (Product) o;
    return getId() != null && Objects.equals(getId(), product.getId());
  }

  @Override
  public final int hashCode() {
    return this instanceof HibernateProxy proxy
        ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
        : getClass().hashCode();
  }
}
