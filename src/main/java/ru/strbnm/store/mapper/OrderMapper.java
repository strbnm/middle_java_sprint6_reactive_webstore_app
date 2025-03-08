package ru.strbnm.store.mapper;

import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.strbnm.store.dto.OrderDto;
import ru.strbnm.store.entity.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  @Mapping(target = "items", expression = "java(new java.util.ArrayList<>())")
  OrderDto toDTO(Order order);

  List<OrderDto> toDTOs(List<Order> orders);
}
