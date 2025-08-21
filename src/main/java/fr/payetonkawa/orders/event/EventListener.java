package fr.payetonkawa.orders.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.payetonkawa.orders.exception.MissingDataException;
import fr.payetonkawa.orders.messaging.ExchangeMessage;
import fr.payetonkawa.orders.messaging.ExchangeQueues;
import fr.payetonkawa.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventListener {

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @RabbitListener(queues = ExchangeQueues.ORDER_QUEUE_NAME)
    public void handleEvent(String rawMessage, Message amqpMessage) throws Exception {
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();

        log.info("📩 Received event: {}", routingKey);

        ExchangeMessage event = objectMapper.readValue(rawMessage, ExchangeMessage.class);
        Map<String, Object> payload = objectMapper.convertValue(
                event.getPayload(),
                new com.fasterxml.jackson.core.type.TypeReference<>() {}
        );

        Long orderId = extractOrderId(payload);

        switch (routingKey) {
            case "product.stock.confirmed" -> updateOrderStatus(orderId, "CONFIRMED");
            case "product.stock.insufficient" -> updateOrderStatus(orderId, "FAILED");
            default -> log.warn("⚠️ Unhandled routing key: {}", routingKey);
        }
    }

    private Long extractOrderId(Map<String, Object> payload) {
        if (!payload.containsKey("orderId")) {
            throw new MissingDataException("Payload missing orderId");
        }
        return Long.valueOf(payload.get("orderId").toString());
    }

    private void updateOrderStatus(Long orderId, String newStatus) {
        orderRepository.findById(orderId).ifPresentOrElse(order -> {
            log.info("🔄 Updating order {} to status '{}'", orderId, newStatus);
            order.setStatus(newStatus);
            orderRepository.save(order);
        }, () -> log.warn("🚫 Order not found for ID: {}", orderId));
    }
}
