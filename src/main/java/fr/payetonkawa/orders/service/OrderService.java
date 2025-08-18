package fr.payetonkawa.orders.service;

import fr.payetonkawa.common.exchange.ExchangeMessage;
import fr.payetonkawa.common.exchange.ExchangeQueues;
import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.dto.OrderItemDto;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.entity.OrderItem;
import fr.payetonkawa.orders.event.EventPublisher;
import fr.payetonkawa.orders.exception.MissingDataException;
import fr.payetonkawa.orders.repository.OrderItemRepository;
import fr.payetonkawa.orders.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;

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
        eventPublisher.sendEvent(EventPublisher.ROUTING_KEY_ORDER_CREATED, ExchangeMessage
                .builder()
                .payload(Map.of(
                        "orderId", savedOrder.getId(),
                        "clientId", savedOrder.getClientId(),
                        "createdAt", savedOrder.getCreatedAt().getTime(),
                        "itemsCost", savedOrder.getItems().stream()
                                .map(item -> item.getUnitPrice() * item.getQuantity())
                                .reduce(0.0, Double::sum)
                ))
                .routingKey(EventPublisher.ROUTING_KEY_ORDER_CREATED)
                .exchangeId(ExchangeQueues.EXCHANGE_NAME)
                .build());
        return OrderDto.fromEntity(savedOrder);
    }

    public void updateProduct(Long productId, double newPrice) {
        List<OrderItem> items = orderItemRepository.findByItemId(productId.toString());
        if (items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            item.setUnitPrice(newPrice);
            orderItemRepository.save(item);
        }
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
        eventPublisher.sendEvent(EventPublisher.ROUTING_KEY_ORDER_UPDATED, ExchangeMessage
                .builder()
                .payload(Map.of(
                        "orderId", order.getId(),
                        "clientId", order.getClientId(),
                        "createdAt", order.getCreatedAt().getTime(),
                        "itemsCost", order.getItems().stream()
                                .map(item -> item.getUnitPrice() * item.getQuantity())
                                .reduce(0.0, Double::sum)
                ))
                .routingKey(EventPublisher.ROUTING_KEY_ORDER_UPDATED)
                .exchangeId(ExchangeQueues.EXCHANGE_NAME)
                .build());
        return OrderDto.fromEntity(orderRepository.save(order));
    }

    public void delete(Long id) {
        orderRepository.deleteById(id);
        eventPublisher.sendEvent(EventPublisher.ROUTING_KEY_ORDER_DELETED, ExchangeMessage
        .builder()
                .payload(Map.of("orderId", id))
                .routingKey(EventPublisher.ROUTING_KEY_ORDER_DELETED)
                .exchangeId(ExchangeQueues.EXCHANGE_NAME)
                .build());
    }

}
