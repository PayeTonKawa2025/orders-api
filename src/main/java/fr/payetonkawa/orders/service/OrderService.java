package fr.payetonkawa.orders.service;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.dto.OrderItemDto;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.entity.OrderItem;
import fr.payetonkawa.orders.event.EventPublisher;
import fr.payetonkawa.orders.exception.MissingDataException;
import fr.payetonkawa.orders.messaging.ExchangeMessage;
import fr.payetonkawa.orders.repository.OrderItemRepository;
import fr.payetonkawa.orders.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EventPublisher eventPublisher;

    public List<OrderDto> getByClientId(String clientId) {
        return OrderDto.fromEntities(orderRepository.findAllByClientId(clientId));
    }


    /** Create order + publish order.created */
    public OrderDto create(OrderDto orderDto) {
        validateOrderInputForCreate(orderDto);

        Order order = new Order();
        order.setClientId(orderDto.getClientId());
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        List<OrderItem> items = orderDto.getItems().stream()
                .map(OrderItemDto::toEntity)
                .collect(Collectors.toList());
        order.setItems(items);

        Order savedOrder = orderRepository.save(order);

        List<Map<String, Object>> itemPayload = mapItemsToPayload(savedOrder.getItems());
        Map<String, Object> payload = Map.of(
                "orderId", savedOrder.getId(),
                "clientId", savedOrder.getClientId(),
                "items", itemPayload
        );

        eventPublisher.sendEvent("order.created", ExchangeMessage.builder()
                .payload(payload)
                .build());

        return OrderDto.fromEntity(savedOrder);
    }


    public OrderDto update(Long orderId, OrderDto orderDto) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new MissingDataException("Order not found"));

        String previousStatus = existingOrder.getStatus();

        if (isCancellationRequest(orderDto) && !"CANCELLED".equalsIgnoreCase(previousStatus)) {
            existingOrder.setStatus("CANCELLED");
            Order saved = orderRepository.save(existingOrder);

            List<Map<String, Object>> itemPayload = mapItemsToPayload(saved.getItems());

            eventPublisher.sendEvent("order.cancelled", ExchangeMessage.builder()
                    .payload(Map.of(
                            "orderId", saved.getId(),
                            "clientId", saved.getClientId(),
                            "items", itemPayload
                    ))
                    .build());

            return OrderDto.fromEntity(saved);
        }


        validateOrderInputForUpdate(orderDto);

        List<Map<String, Object>> previousItems = mapItemsToPayload(existingOrder.getItems());

        existingOrder.setClientId(orderDto.getClientId()); // clientId is required
        List<OrderItem> newItems = orderDto.getItems().stream()
                .map(OrderItemDto::toEntity)
                .collect(Collectors.toList());
        existingOrder.setItems(newItems);

        Order updatedOrder = orderRepository.save(existingOrder);

        List<Map<String, Object>> newItemPayload = mapItemsToPayload(updatedOrder.getItems());

        eventPublisher.sendEvent("order.updated", ExchangeMessage.builder()
                .payload(Map.of(
                        "orderId", updatedOrder.getId(),
                        "clientId", updatedOrder.getClientId(),
                        "previousItems", previousItems,
                        "items", newItemPayload
                ))
                .build());

        return OrderDto.fromEntity(updatedOrder);
    }

    /** Delete order (idempotent) + publish order.deleted only if it existed */
    public void delete(Long id) {
        Optional<Order> opt = orderRepository.findById(id);

        boolean existed = opt.isPresent();
        boolean wasCancelled = existed && "CANCELLED".equalsIgnoreCase(opt.get().getStatus());

        // on capture le payload uniquement si on DOIT publier
        List<Map<String, Object>> itemPayload = (!wasCancelled && existed)
                ? mapItemsToPayload(opt.get().getItems())
                : null;

        orderRepository.deleteById(id);

        if (itemPayload != null) {
            eventPublisher.sendEvent("order.deleted", ExchangeMessage.builder()
                    .payload(Map.of(
                            "orderId", id,
                            "items", itemPayload
                    ))
                    .build());
        }
    }


    private void validateOrderInputForCreate(OrderDto orderDto) {
        if (orderDto.getClientId() == null || orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new MissingDataException("Client ID and items must be provided");
        }
    }

    private void validateOrderInputForUpdate(OrderDto orderDto) {
        if (orderDto.getClientId() == null) {
            throw new MissingDataException("Client ID must be provided");
        }
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new MissingDataException("Items cannot be null when updating an order");
        }
    }

    private boolean isCancellationRequest(OrderDto orderDto) {
        return orderDto.getStatus() != null && "CANCELLED".equalsIgnoreCase(orderDto.getStatus());
    }


    private List<Map<String, Object>> mapItemsToPayload(List<OrderItem> items) {
        if (items == null) return List.of();
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", item.getItemId());
            map.put("quantity", item.getQuantity());
            return map;
        }).collect(Collectors.toList());
    }
}
