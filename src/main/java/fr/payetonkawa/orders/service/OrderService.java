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

    /**
     * Retrieve all orders of a given client
     */
    public List<OrderDto> getByClientId(String clientId) {
        return OrderDto.fromEntities(orderRepository.findAllByClientId(clientId));
    }

    /**
     * Create a new order and publish "order.created"
     */
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

        Map<String, Object> eventPayload = Map.of(
                "orderId", savedOrder.getId(),
                "clientId", savedOrder.getClientId(),
                "items", itemPayload
        );

        eventPublisher.sendEvent("order.created", ExchangeMessage.builder()
                .payload(eventPayload)
                .build());

        return OrderDto.fromEntity(savedOrder);
    }

    /**
     * Update an existing order:
     *  - if status is set to CANCELLED (and wasn't before): publish "order.cancelled" (restock) and keep order
     *  - else update items and publish "order.updated" (delta stock will be handled by products)
     */
    public OrderDto update(Long orderId, OrderDto orderDto) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new MissingDataException("Order not found"));

        String previousStatus = existingOrder.getStatus();

        // 1) Gestion de l'annulation (ne nécessite pas les items)
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

        // 2) Mise à jour "classique" des items
        validateOrderInputForUpdate(orderDto);

        List<Map<String, Object>> previousItems = mapItemsToPayload(existingOrder.getItems());

        List<OrderItem> newItems = orderDto.getItems().stream()
                .map(OrderItemDto::toEntity)
                .collect(Collectors.toList());

        // (optionnel) MAJ du client si on veut autoriser le changement
        if (orderDto.getClientId() != null) {
            existingOrder.setClientId(orderDto.getClientId());
        }
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

    /**
     * Delete an order and publish "order.deleted"
     */
    public void delete(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new MissingDataException("Order not found"));

        List<Map<String, Object>> itemPayload = mapItemsToPayload(order.getItems());

        orderRepository.deleteById(id);

        eventPublisher.sendEvent("order.deleted", ExchangeMessage.builder()
                .payload(Map.of(
                        "orderId", id,
                        "items", itemPayload
                ))
                .build());
    }

    /**
     * Update unit price of all order items for a specific product
     */
    public void updateProduct(Long productId, double newPrice) {
        List<OrderItem> items = orderItemRepository.findByItemId(productId.toString());
        for (OrderItem item : items) {
            item.setUnitPrice(newPrice);
            orderItemRepository.save(item);
        }
    }

    /*** === Validations === ***/
    private void validateOrderInputForCreate(OrderDto orderDto) {
        if (orderDto.getClientId() == null || orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new MissingDataException("Client ID and items must be provided");
        }
    }

    private void validateOrderInputForUpdate(OrderDto orderDto) {
        // pour update classique, items requis
        if (orderDto.getItems() == null || orderDto.getItems().isEmpty()) {
            throw new MissingDataException("Items cannot be null when updating an order");
        }
    }

    private boolean isCancellationRequest(OrderDto orderDto) {
        return orderDto.getStatus() != null && "CANCELLED".equalsIgnoreCase(orderDto.getStatus());
    }

    /**
     * Convert list of OrderItems to List<Map<String, Object>> for event payload
     */
    private List<Map<String, Object>> mapItemsToPayload(List<OrderItem> items) {
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("itemId", item.getItemId());
            map.put("quantity", item.getQuantity());
            return map;
        }).collect(Collectors.toList());
    }
}
