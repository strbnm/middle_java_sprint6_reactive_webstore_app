package ru.strbnm.store.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.OrderDTO;
import ru.strbnm.store.entity.Order;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);
    OrderDTO toDTO(Order order);
    List<OrderDTO> toDTOs(List<Order> orders);
}