package fr.payetonkawa.orders.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.messaging.ExchangeMessage;
import fr.payetonkawa.orders.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private EventListener eventListener;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldUpdateOrderStatusToConfirmed() throws Exception {
        // Given
        Order mockOrder = new Order();
        mockOrder.setStatus("PENDING");
        when(orderRepository.findById(1L)).thenReturn(Optional.of(mockOrder));

        ExchangeMessage exchangeMessage = ExchangeMessage.builder()
                .payload(Map.of("orderId", 1L))
                .build();

        String jsonMessage = objectMapper.writeValueAsString(exchangeMessage);

        Message amqpMessage = mock(Message.class);
        MessageProperties props = mock(MessageProperties.class);
        when(amqpMessage.getMessageProperties()).thenReturn(props);
        when(props.getReceivedRoutingKey()).thenReturn("product.stock.confirmed");

        // When
        eventListener.handleEvent(jsonMessage, amqpMessage);

        // Then
        verify(orderRepository).save(mockOrder);
        assert mockOrder.getStatus().equals("CONFIRMED");
    }

    @Test
    void shouldUpdateOrderStatusToFailed() throws Exception {
        // Given
        Order mockOrder = new Order();
        mockOrder.setStatus("PENDING");
        when(orderRepository.findById(2L)).thenReturn(Optional.of(mockOrder));

        ExchangeMessage exchangeMessage = ExchangeMessage.builder()
                .payload(Map.of("orderId", 2L))
                .build();

        String jsonMessage = objectMapper.writeValueAsString(exchangeMessage);

        Message amqpMessage = mock(Message.class);
        MessageProperties props = mock(MessageProperties.class);
        when(amqpMessage.getMessageProperties()).thenReturn(props);
        when(props.getReceivedRoutingKey()).thenReturn("product.stock.insufficient");

        // When
        eventListener.handleEvent(jsonMessage, amqpMessage);

        // Then
        verify(orderRepository).save(mockOrder);
        assert mockOrder.getStatus().equals("FAILED");
    }

    @Test
    void shouldHandleUnknownRoutingKeyWithoutSaving() throws Exception {
        // Given
        ExchangeMessage exchangeMessage = ExchangeMessage.builder()
                .payload(Map.of("orderId", 999L))
                .build();

        String jsonMessage = objectMapper.writeValueAsString(exchangeMessage);

        Message amqpMessage = mock(Message.class);
        MessageProperties props = mock(MessageProperties.class);
        when(amqpMessage.getMessageProperties()).thenReturn(props);
        when(props.getReceivedRoutingKey()).thenReturn("unknown.event");

        // When
        eventListener.handleEvent(jsonMessage, amqpMessage);

        // Then
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionOnMalformedJson() {
        // Given
        String badJson = "not a json";
        Message amqpMessage = mock(Message.class);
        MessageProperties props = mock(MessageProperties.class);
        when(amqpMessage.getMessageProperties()).thenReturn(props);
        when(props.getReceivedRoutingKey()).thenReturn("product.stock.confirmed");

        // Then
        assertThrows(Exception.class, () -> eventListener.handleEvent(badJson, amqpMessage));
    }
}
