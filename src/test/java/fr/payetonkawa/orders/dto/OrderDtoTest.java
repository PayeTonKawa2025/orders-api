package fr.payetonkawa.orders.dto;

import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.entity.OrderItem;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderDtoTest {

    @Test
    void shouldMapFieldsWhenFromEntity() {
        Order order = new Order();
        order.setId(1L);
        order.setClientId("client-1");
        order.setCreatedAt(new Timestamp(123456789L));
        OrderItem item = new OrderItem();
        item.setId(2L);
        item.setItemId("item-1");
        item.setQuantity(3);
        item.setUnitPrice(9.99);
        order.setItems(List.of(item));

        OrderDto dto = OrderDto.fromEntity(order);

        assertEquals(order.getId(), dto.getId());
        assertEquals(order.getClientId(), dto.getClientId());
        assertEquals(order.getCreatedAt().getTime(), dto.getCreatedAt());
        assertNotNull(dto.getItems());
        assertEquals(1, dto.getItems().size());
        assertEquals(item.getId(), dto.getItems().get(0).getId());
    }

    @Test
    void shouldMapListWhenFromEntities() {
        Order order1 = new Order();
        order1.setId(1L);
        order1.setClientId("client-1");
        order1.setCreatedAt(new Timestamp(1L));
        order1.setItems(List.of());

        Order order2 = new Order();
        order2.setId(2L);
        order2.setClientId("client-2");
        order2.setCreatedAt(new Timestamp(2L));
        order2.setItems(List.of());

        List<OrderDto> dtos = OrderDto.fromEntities(List.of(order1, order2));
        assertEquals(2, dtos.size());
        assertEquals(order1.getId(), dtos.get(0).getId());
        assertEquals(order2.getId(), dtos.get(1).getId());
    }
}