package fr.payetonkawa.orders.event;

import fr.payetonkawa.orders.config.RabbitMQConfig;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventListener {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleEvent(String message, Message amqpMessage) {
        String routingKey = amqpMessage.getMessageProperties().getReceivedRoutingKey();
        System.out.println("Received event: " + message + " from " + routingKey);
    }

}
