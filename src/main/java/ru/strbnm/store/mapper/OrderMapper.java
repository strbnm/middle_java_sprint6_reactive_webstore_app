package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  OrderDto toDTO(Order order);

  List<OrderDto> toDTOs(List<Order> orders);
}
