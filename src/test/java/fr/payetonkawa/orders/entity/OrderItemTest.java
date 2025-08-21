package fr.payetonkawa.orders.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderItemTest {

    @Test
    void testDefaultQuantity() {
        OrderItem item = new OrderItem();
        assertEquals(1, item.getQuantity());
    }

    @Test
    void testSettersAndGetters() {
        OrderItem item = new OrderItem();
        item.setId(10L);
        item.setItemId("ABC123");
        item.setQuantity(5);
        item.setUnitPrice(9.99);

        assertEquals(10L, item.getId());
        assertEquals("ABC123", item.getItemId());
        assertEquals(5, item.getQuantity());
        assertEquals(9.99, item.getUnitPrice());
    }

    @Test
    void testOrderRelationship() {
        Order order = new Order();
        OrderItem item = new OrderItem();
        item.setOrder(order);
        assertEquals(order, item.getOrder());
    }

    @Test
    void testEqualsSameId() {
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        item1.setId(1L);
        item2.setId(1L);
        assertEquals(item1, item2);
    }

    @Test
    void testEqualsDifferentId() {
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        item1.setId(1L);
        item2.setId(2L);
        assertNotEquals(item1, item2);
    }

    @Test
    void testEqualsNullId() {
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        assertNotEquals(item1, item2);
    }

    @Test
    void testEqualsSameInstance() {
        OrderItem item = new OrderItem();
        assertEquals(item, item);
    }

    @Test
    void testEqualsDifferentClass() {
        OrderItem item = new OrderItem();
        assertNotEquals(item, "not-an-orderitem");
    }

    @Test
    void testHashCodeAlways31() {
        OrderItem item1 = new OrderItem();
        OrderItem item2 = new OrderItem();
        assertEquals(31, item1.hashCode());
        assertEquals(31, item2.hashCode());
    }
}