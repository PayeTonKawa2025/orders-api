package fr.payetonkawa.orders.dto;

import fr.payetonkawa.orders.entity.OrderItem;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderItemDtoTest {

    @Test
    void shouldMapFieldsWhenFromEntity() {
        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setItemId("item-1");
        item.setQuantity(2);
        item.setUnitPrice(5.5);

        OrderItemDto dto = OrderItemDto.fromEntity(item);

        assertEquals(item.getId(), dto.getId());
        assertEquals(item.getItemId(), dto.getItemId());
        assertEquals(item.getQuantity(), dto.getQuantity());
        assertEquals(item.getUnitPrice(), dto.getUnitPrice());
    }

    @Test
    void shouldMapListWhenFromEntities() {
        OrderItem item1 = new OrderItem();
        item1.setId(1L);
        item1.setItemId("item-1");
        item1.setQuantity(1);
        item1.setUnitPrice(2.0);

        OrderItem item2 = new OrderItem();
        item2.setId(2L);
        item2.setItemId("item-2");
        item2.setQuantity(2);
        item2.setUnitPrice(3.0);

        List<OrderItemDto> dtos = OrderItemDto.fromEntities(List.of(item1, item2));
        assertEquals(2, dtos.size());
        assertEquals(item1.getId(), dtos.get(0).getId());
        assertEquals(item2.getId(), dtos.get(1).getId());
    }

    @Test
    void shouldMapFieldsWhenToEntity() {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(1L);
        dto.setItemId("item-1");
        dto.setQuantity(2);
        dto.setUnitPrice(5.5);

        OrderItem item = dto.toEntity();

        assertEquals(dto.getId(), item.getId());
        assertEquals(dto.getItemId(), item.getItemId());
        assertEquals(dto.getQuantity(), item.getQuantity());
        assertEquals(dto.getUnitPrice(), item.getUnitPrice());
    }

    @Test
    void shouldThrowExceptionWhenToEntityWithInvalidItemId() {
        OrderItemDto dto = new OrderItemDto();
        dto.setItemId("");
        dto.setQuantity(1);
        dto.setUnitPrice(1.0);

        assertThrows(IllegalArgumentException.class, dto::toEntity);
    }

    @Test
    void shouldThrowExceptionWhenToEntityWithInvalidQuantity() {
        OrderItemDto dto = new OrderItemDto();
        dto.setItemId("item-1");
        dto.setQuantity(0);
        dto.setUnitPrice(1.0);

        assertThrows(IllegalArgumentException.class, dto::toEntity);
    }

    @Test
    void shouldThrowExceptionWhenToEntityWithNegativeUnitPrice() {
        OrderItemDto dto = new OrderItemDto();
        dto.setItemId("item-1");
        dto.setQuantity(1);
        dto.setUnitPrice(-1.0);

        assertThrows(IllegalArgumentException.class, dto::toEntity);
    }
}