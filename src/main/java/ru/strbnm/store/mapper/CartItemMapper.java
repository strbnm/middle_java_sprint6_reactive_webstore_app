package ru.strbnm.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.CartItemDTO;
import ru.strbnm.store.entity.CartItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);
    CartItemDTO toDTO(CartItem cartItem);
    List<CartItemDTO> toDTOs(List<CartItem> cartItems);
}