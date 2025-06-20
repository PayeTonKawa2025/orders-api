package fr.payetonkawa.orders.service;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.dto.OrderItemDto;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.entity.OrderItem;
import fr.payetonkawa.orders.exception.MissingDataException;
import fr.payetonkawa.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private static final List<Order> ORDERS = new ArrayList<>();

    @BeforeEach
    void setUp() {
        ORDERS.clear();

        ORDERS.add(buildOrder(1L, "1", 3));
        ORDERS.add(buildOrder(2L, "3", 4));
        ORDERS.add(buildOrder(3L, "1", 1));
        ORDERS.add(buildOrder(4L, "1", 2));
    }

    @Test
    void shouldReturnListOfOrdersDtoWhenGetByClientId() {
        // Given
        String clientId = "1";

        // When
        when(orderRepository.findAllByClientId(clientId)).thenReturn(ORDERS
                .stream().filter(order -> clientId.equals(order.getClientId())).toList());

        // Test
        List<OrderDto> orders = orderService.getByClientId(clientId);

        // Then
        assertNotNull(orders, "Returned list should not be null");
        assertEquals(3, orders.size());
    }

    @Test
    void shouldReturnListOfOrdersDtoWhenGetByClientIdAndListEmpty() {
        // Given
        String clientId = "2";

        // When
        when(orderRepository.findAllByClientId(clientId)).thenReturn(new ArrayList<>());

        // Test
        List<OrderDto> orders = orderService.getByClientId(clientId);

        // Then
        assertNotNull(orders, "Returned list should not be null");
        assertTrue(orders.isEmpty(), "Expected empty list for clientId '2'");
    }

    @Test
    void shouldCreateOrderSuccessfully() {
        // Given
        OrderDto orderDto = new OrderDto();
        orderDto.setClientId("client-1");
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setItemId("item-1");
        itemDto.setQuantity(2);
        itemDto.setUnitPrice(5.0);
        orderDto.setItems(List.of(itemDto));

        Order savedOrder = buildOrder(10L, "client-1", 1);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        // When
        OrderDto result = orderService.create(orderDto);

        // Then
        assertNotNull(result);
        assertEquals(savedOrder.getId(), result.getId());
        assertEquals(savedOrder.getClientId(), result.getClientId());
    }

    @Test
    void shouldThrowExceptionWhenCreateOrderWithMissingData() {
        // Given
        OrderDto orderDto = new OrderDto();
        orderDto.setClientId(null);
        orderDto.setItems(null);

        // Then
        assertThrows(MissingDataException.class, () -> orderService.create(orderDto));
    }

    @Test
    void shouldUpdateOrderSuccessfully() {
        // Given
        Long orderId = 1L;
        Order existingOrder = buildOrder(orderId, "client-1", 1);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);

        OrderDto orderDto = new OrderDto();
        orderDto.setClientId("client-1");
        OrderItemDto itemDto = new OrderItemDto();
        itemDto.setItemId("item-2");
        itemDto.setQuantity(3);
        itemDto.setUnitPrice(7.0);
        orderDto.setItems(List.of(itemDto));

        // When
        OrderDto result = orderService.update(orderId, orderDto);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals("client-1", result.getClientId());
    }

    @Test
    void shouldThrowExceptionWhenUpdateOrderNotFound() {
        // Given
        Long orderId = 99L;
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.empty());

        OrderDto orderDto = new OrderDto();
        orderDto.setClientId("client-1");
        orderDto.setItems(List.of());

        // Then
        assertThrows(MissingDataException.class, () -> orderService.update(orderId, orderDto));
    }

    @Test
    void shouldThrowExceptionWhenUpdateOrderWithMissingData() {
        // Given
        Long orderId = 1L;
        Order existingOrder = buildOrder(orderId, "client-1", 1);
        when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(existingOrder));

        OrderDto orderDto = new OrderDto();
        orderDto.setClientId(null);
        orderDto.setItems(null);

        // Then
        assertThrows(MissingDataException.class, () -> orderService.update(orderId, orderDto));
    }

    @Test
    void shouldDeleteOrder() {
        // Given
        Long orderId = 1L;

        // When
        orderService.delete(orderId);

        // Then
        verify(orderRepository, times(1)).deleteById(orderId);
    }

    private static Order buildOrder(Long id, String clientId, int numbersOfItems) {
        Order order = new Order();
        order.setId(id);
        order.setClientId(clientId);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < numbersOfItems; i++) {
            items.add(buildOrderItem(i));
        }
        order.setItems(items);

        return order;
    }

    private static OrderItem buildOrderItem(int index) {
        OrderItem item = new OrderItem();
        item.setId((long) index);
        item.setItemId("item-" + index);
        item.setQuantity(1);
        item.setUnitPrice(10.0 + index); // Example price
        return item;
    }

}
