package fr.payetonkawa.orders.config;

import fr.payetonkawa.orders.messaging.ExchangeQueues;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(ExchangeQueues.EXCHANGE_NAME);
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(ExchangeQueues.ORDER_QUEUE_NAME);
    }

    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue).to(exchange).with("product.#");
    }
}
