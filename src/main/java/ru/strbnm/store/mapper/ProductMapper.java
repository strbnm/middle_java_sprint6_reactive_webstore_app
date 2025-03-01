package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ru.strbnm.store.dto.ProductDto;
import ru.strbnm.store.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  ProductDto toDTO(Product product);

  List<ProductDto> toDTOs(List<Product> products);
}
