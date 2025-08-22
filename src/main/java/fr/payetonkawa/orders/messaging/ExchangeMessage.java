package fr.payetonkawa.orders.messaging;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeMessage {
    private String exchangeId;
    private String routingKey;
    private String type;
    private String correlationId;
    private Object payload;
}