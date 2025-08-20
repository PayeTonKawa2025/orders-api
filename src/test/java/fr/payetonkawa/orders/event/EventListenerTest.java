package fr.payetonkawa.orders.event;

import com.google.gson.Gson;
import fr.payetonkawa.common.exchange.ExchangeMessage;
import fr.payetonkawa.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private EventListener eventListener;

    private final Gson gson = new Gson();

    @Test
    void shouldHandleOtherEventWithoutCallingUpdateProduct() {
        // Given
        ExchangeMessage exchangeMessage = ExchangeMessage.builder()
                .routingKey("other.event")
                .payload(Map.of("data", "test"))
                .build();

        String jsonMessage = gson.toJson(exchangeMessage);
        Message amqpMessage = mock(Message.class);
        MessageProperties messageProperties = mock(MessageProperties.class);

        when(amqpMessage.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getReceivedRoutingKey()).thenReturn("other.event");

        // When
        eventListener.handleEvent(jsonMessage, amqpMessage);

        // Then
        verify(orderService, never()).updateProduct(any(), anyDouble());
    }

    @Test
    void shouldThrowExceptionWhenInvalidJsonMessage() {
        // Given
        String invalidJsonMessage = "invalid json";
        Message amqpMessage = mock(Message.class);
        MessageProperties messageProperties = mock(MessageProperties.class);

        when(amqpMessage.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getReceivedRoutingKey()).thenReturn("test.routing.key");

        // When & Then
        assertThrows(com.google.gson.JsonSyntaxException.class,
                () -> eventListener.handleEvent(invalidJsonMessage, amqpMessage));
        verify(orderService, never()).updateProduct(any(), anyDouble());
    }

    @Test
    void shouldHandleEmptyPayloadMessage() {
        // Given
        ExchangeMessage exchangeMessage = ExchangeMessage.builder()
                .routingKey("product.price.updated")
                .payload(null)
                .build();

        String jsonMessage = gson.toJson(exchangeMessage);
        Message amqpMessage = mock(Message.class);
        MessageProperties messageProperties = mock(MessageProperties.class);

        when(amqpMessage.getMessageProperties()).thenReturn(messageProperties);
        when(messageProperties.getReceivedRoutingKey()).thenReturn("product.price.updated");

        // When
        eventListener.handleEvent(jsonMessage, amqpMessage);

        // Then
        verify(orderService, never()).updateProduct(any(), anyDouble());
    }
}