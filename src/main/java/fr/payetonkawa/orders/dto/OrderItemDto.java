package fr.payetonkawa.orders.dto;

import fr.payetonkawa.orders.entity.OrderItem;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderItemDto {

    private Long id;
    private String itemId;
    private int quantity = 1;
    private double unitPrice;

    public static OrderItemDto fromEntity(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setItemId(item.getItemId());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        return dto;
    }

    public static List<OrderItemDto> fromEntities(List<OrderItem> items) {
        return items.stream()
                .map(OrderItemDto::fromEntity)
                .toList();
    }

}
