package ru.strbnm.store.entity;

import java.math.BigDecimal;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("products")
public class Product {
  @Id
  private Long id;
  
  private String name;
  
  private String description;

  @Column("image_url")
  private String imageUrl;

  private BigDecimal price;
}
