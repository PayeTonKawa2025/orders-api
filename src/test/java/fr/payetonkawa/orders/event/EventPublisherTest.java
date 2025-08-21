package fr.payetonkawa.orders.event;

import com.google.gson.Gson;
import fr.payetonkawa.orders.messaging.ExchangeMessage;
import fr.payetonkawa.orders.messaging.ExchangeQueues;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.AmqpTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventPublisherTest {

    private AmqpTemplate amqpTemplate;
    private EventPublisher eventPublisher;
    private Gson gson;

    @BeforeEach
    void setUp() {
        amqpTemplate = mock(AmqpTemplate.class);
        eventPublisher = new EventPublisher(amqpTemplate);
        gson = new Gson();
    }

    @Test
    void sendEvent_shouldSetFieldsAndPublishMessage() {
        // Arrange
        String routingKey = "order.created";
        ExchangeMessage message = new ExchangeMessage();
        message.setPayload("test-payload");

        // Act
        eventPublisher.sendEvent(routingKey, message);

        // Assert
        assertEquals(ExchangeQueues.EXCHANGE_NAME, message.getExchangeId());
        assertEquals(routingKey, message.getRoutingKey());
        assertEquals(routingKey, message.getType());

        ArgumentCaptor<String> exchangeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> routingKeyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(amqpTemplate, times(1)).convertAndSend(
                exchangeCaptor.capture(),
                routingKeyCaptor.capture(),
                payloadCaptor.capture()
        );

        assertEquals(ExchangeQueues.EXCHANGE_NAME, exchangeCaptor.getValue());
        assertEquals(routingKey, routingKeyCaptor.getValue());

        // Check that the payload is the JSON representation of the message
        String expectedJson = gson.toJson(message);
        assertEquals(expectedJson, payloadCaptor.getValue());
    }
}