package fr.payetonkawa.orders.service;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.dto.OrderItemDto;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.entity.OrderItem;
import fr.payetonkawa.orders.exception.MissingDataException;
import fr.payetonkawa.orders.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderDto> getByClientId(String clientId) {
        List<Order> orders = orderRepository.findAllByClientId(clientId);
        return OrderDto.fromEntities(orders);
    }

    public OrderDto create(OrderDto orderDto) {
        if (orderDto.getClientId() == null || orderDto.getItems() == null) {
            throw new MissingDataException("Client ID and items cannot be null");
        }
        Order order = new Order();
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        order.setClientId(orderDto.getClientId());
        List<OrderItem> items = orderDto.getItems().stream().map(OrderItemDto::toEntity).toList();
        order.setItems(items);
        Order savedOrder = orderRepository.save(order);
        return OrderDto.fromEntity(savedOrder);
    }

    public OrderDto update(Long id, OrderDto orderDto) {
        Order order = orderRepository.findById(id).orElse(null);
        if (order == null)
            throw new MissingDataException("Order not found");
        if (orderDto.getClientId() == null || orderDto.getItems() == null) {
            throw new MissingDataException("Client ID and items cannot be null");
        }
        List<OrderItem> items = orderDto.getItems().stream().map(OrderItemDto::toEntity).toList();
        order.setItems(items);
        return OrderDto.fromEntity(orderRepository.save(order));
    }

    public void delete(Long id) {
        orderRepository.deleteById(id);
    }

}
