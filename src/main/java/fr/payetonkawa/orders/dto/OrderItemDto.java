package fr.payetonkawa.orders.dto;

import fr.payetonkawa.orders.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class OrderItemDto {

    private Long id;
    private String itemId;
    private int quantity = 1;
    private Double unitPrice;

    public static OrderItemDto fromEntity(OrderItem item) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setItemId(item.getItemId());
        dto.setQuantity(item.getQuantity());
        if (item.getUnitPrice() == null) {
            dto.setUnitPrice(0.0);
        } else {
            dto.setUnitPrice(item.getUnitPrice());
        }
        return dto;
    }

    public static List<OrderItemDto> fromEntities(List<OrderItem> items) {
        return items.stream()
                .map(OrderItemDto::fromEntity)
                .toList();
    }

    public OrderItem toEntity() {
        OrderItem item = new OrderItem();
        item.setId(this.id);
        if (this.itemId == null || this.itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID cannot be null or blank");
        }
        item.setItemId(this.itemId);
        if (this.quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        item.setQuantity(this.quantity);
        if (this.unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        item.setUnitPrice(this.unitPrice);
        return item;
    }

}
