package fr.payetonkawa.orders.dto;

import fr.payetonkawa.orders.entity.Order;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto {

    private Long id;
    private String clientId;
    private Long createdAt;
    private List<OrderItemDto> items;

    public static OrderDto fromEntity(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setClientId(order.getClientId());
        dto.setCreatedAt(order.getCreatedAt().getTime());
        dto.setItems(OrderItemDto.fromEntities(order.getItems()));
        return dto;
    }

    public static List<OrderDto> fromEntities(List<Order> orders) {
        return orders.stream()
                .map(OrderDto::fromEntity)
                .toList();
    }

}
