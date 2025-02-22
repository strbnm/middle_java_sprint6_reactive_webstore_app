package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  OrderDto toDTO(Order order);

  List<OrderDto> toDTOs(List<Order> orders);
}
