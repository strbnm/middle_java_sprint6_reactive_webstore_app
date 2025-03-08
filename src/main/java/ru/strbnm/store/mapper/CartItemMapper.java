package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ru.strbnm.store.dto.CartItemDto;
import ru.strbnm.store.entity.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
  CartItemDto toDto(CartItem cartItem);

  CartItem toEntity(CartItemDto cartItemDto);

  List<CartItemDto> toDTOs(List<CartItem> cartItems);
}
