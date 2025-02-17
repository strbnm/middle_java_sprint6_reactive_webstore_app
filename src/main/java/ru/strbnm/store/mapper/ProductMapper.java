package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.ProductDTO;
import ru.strbnm.store.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
  ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

  ProductDTO toDTO(Product product);

  List<ProductDTO> toDTOs(List<Product> products);
}
