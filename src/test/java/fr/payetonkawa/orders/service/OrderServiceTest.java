package fr.payetonkawa.orders.service;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.entity.OrderItem;
import fr.payetonkawa.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private static final List<Order> ORDERS = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        ORDERS.clear();

        ORDERS.add(buildOrder(1L, "1", 3));
        ORDERS.add(buildOrder(2L, "3", 4));
        ORDERS.add(buildOrder(3L, "1", 1));
        ORDERS.add(buildOrder(4L, "1", 2));
    }

    @Test
    public void shouldReturnListOfOrdersDtoWhenGetByClientId() {
        // Given
        String clientId = "1";

        // When
        when(orderRepository.findAllByClientId(clientId)).thenReturn(ORDERS
                .stream().filter(order -> clientId.equals(order.getClientId())).toList());

        // Test
        List<OrderDto> orders = orderService.getByClientId(clientId);

        // Then
        assert orders.size() == 3 : "Expected 3 orders for clientId '1'";
    }

    @Test
    public void shouldReturnListOfOrdersDtoWhenGetByClientIdAndListEmpty() {
        // Given
        String clientId = "2";

        // When
        when(orderRepository.findAllByClientId(clientId)).thenReturn(new ArrayList<>());

        // Test
        List<OrderDto> orders = orderService.getByClientId(clientId);

        // Then
        assert orders.isEmpty() : "Expected empty list for clientId '2'";
    }

    private static Order buildOrder(Long id, String clientId, int numbersOfItems) {
        Order order = new Order();
        order.setId(id);
        order.setClientId(clientId);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < numbersOfItems; i++) {
            items.add(new OrderItem());
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
