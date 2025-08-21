package fr.payetonkawa.orders.event;

import com.google.gson.Gson;
import fr.payetonkawa.orders.messaging.ExchangeMessage;
import fr.payetonkawa.orders.messaging.ExchangeQueues;
import org.springframework.amqp.core.AmqpTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EventPublisher {

    private final AmqpTemplate amqpTemplate;
    private final Gson gson = new Gson();

    public EventPublisher(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendEvent(String routingKey, ExchangeMessage message) {
        message.setExchangeId(ExchangeQueues.EXCHANGE_NAME);
        message.setRoutingKey(routingKey);
        message.setType(routingKey);
        amqpTemplate.convertAndSend(ExchangeQueues.EXCHANGE_NAME, routingKey, gson.toJson(message));
        log.info("📤 Publishing to exchange '{}' with routingKey '{}' and payload '{}'",
                ExchangeQueues.EXCHANGE_NAME, routingKey, gson.toJson(message));
    }
}
