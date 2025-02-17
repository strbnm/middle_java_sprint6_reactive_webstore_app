package ru.strbnm.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.OrderItemDTO;
import ru.strbnm.store.entity.OrderItem;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);
    OrderItemDTO toDTO(OrderItem orderItem);
    List<OrderItemDTO> toDTOs(List<OrderItem> orderItems);
}