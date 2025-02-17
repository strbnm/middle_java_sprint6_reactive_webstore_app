package ru.strbnm.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.ProductDTO;
import ru.strbnm.store.entity.Product;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);
    ProductDTO toDTO(Product product);
    List<ProductDTO> toDTOs(List<Product> products);
}