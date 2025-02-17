package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.strbnm.store.dto.OrderItemDTO;
import ru.strbnm.store.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
  OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.id", target = "productId")
  OrderItemDTO toDTO(OrderItem orderItem);

  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.id", target = "productId")
  List<OrderItemDTO> toDTOs(List<OrderItem> orderItems);
}
