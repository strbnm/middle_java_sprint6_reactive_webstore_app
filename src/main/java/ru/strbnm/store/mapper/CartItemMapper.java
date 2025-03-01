package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
  @Mapping(source = "product.id", target = "productId")
  CartItemDto toDto(CartItem cartItem);

  @Mapping(source = "productId", target = "product.id")
  CartItem toEntity(CartItemDto cartItemDto);

  @Mapping(source = "productId", target = "product.id")
  List<CartItemDto> toDTOs(List<CartItem> cartItems);
}
