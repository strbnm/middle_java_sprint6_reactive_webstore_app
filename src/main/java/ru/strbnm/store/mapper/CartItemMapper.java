package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.CartItemDTO;
import ru.strbnm.store.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
  CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.price", target = "productPrice")
  CartItemDTO toDTO(CartItem cartItem);

  @Mapping(source = "productImageUrl", target = "product.imageUrl")
  @Mapping(source = "productName", target = "product.name")
  @Mapping(source = "productId", target = "product.id")
  @Mapping(source = "productPrice", target = "product.price")
  List<CartItem> toEntity(List<CartItemDTO> cartItemDTO);

  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.id", target = "productId")
  @Mapping(source = "product.price", target = "productPrice")
  List<CartItemDTO> toDTOs(List<CartItem> cartItems);

  @Mapping(source = "productImageUrl", target = "product.imageUrl")
  @Mapping(source = "productName", target = "product.name")
  @Mapping(source = "productId", target = "product.id")
  @Mapping(source = "productPrice", target = "product.price")
  CartItem toEntity(CartItemDTO cartItemDTO);
}
