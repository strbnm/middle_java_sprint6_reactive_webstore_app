package ru.strbnm.store.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.strbnm.store.dto.OrderItemDto;
import ru.strbnm.store.entity.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.id", target = "productId")
  OrderItemDto toDTO(OrderItem orderItem);

  @Mapping(source = "product.imageUrl", target = "productImageUrl")
  @Mapping(source = "product.name", target = "productName")
  @Mapping(source = "product.id", target = "productId")
  List<OrderItemDto> toDTOs(List<OrderItem> orderItems);
}
