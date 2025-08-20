package fr.payetonkawa.orders.event;

import com.google.gson.Gson;
import fr.payetonkawa.common.exchange.ExchangeMessage;
import fr.payetonkawa.orders.config.RabbitMQConfig;
import fr.payetonkawa.orders.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static fr.payetonkawa.common.exchange.ExchangeQueues.ORDER_QUEUE_NAME;

@Component
@Log4j2
@AllArgsConstructor
public class EventListener {

    private final OrderService orderService;

    private static final Gson gson = new Gson();

    @RabbitListener(queues = ORDER_QUEUE_NAME)
    public void handleEvent(String message, Message amqpMessage) {
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();
        ExchangeMessage exchangeMessage = gson.fromJson(message, ExchangeMessage.class);
        if (exchangeMessage == null || exchangeMessage.getPayload() == null) {
            log.warn("Received empty or invalid message: {}", message);
            return;
        }
        log.info("Received event with routing key: {} from queue: {}", routingKey, ORDER_QUEUE_NAME);

        if (exchangeMessage.getRoutingKey().equalsIgnoreCase("product.price.updated")) {
            Long productId = (Long) exchangeMessage.getPayload().get("productId");
            double newPrice = (double) exchangeMessage.getPayload().get("newPrice");
            orderService.updateProduct(productId, newPrice);
        }
    }

}
