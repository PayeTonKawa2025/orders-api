package fr.payetonkawa.orders.controller;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1")
@AllArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{clientId}")
    public List<OrderDto> getOrdersByClientId(@PathVariable String clientId) {
        return orderService.getByClientId(clientId);
    }

    @PostMapping
    public void createOrder(@RequestBody OrderDto orderDto) {
        // This method is a placeholder for creating an order.
        // The actual implementation would involve saving the order to the database
        // and publishing an event to RabbitMQ.
        throw new UnsupportedOperationException("Order creation not implemented yet.");
    }

    @PatchMapping("/{orderId}")
    public void updateOrder(@PathVariable Long orderId, @RequestBody OrderDto orderDto) {
        // This method is a placeholder for updating an order.
        // The actual implementation would involve updating the order in the database
        // and publishing an event to RabbitMQ.
        throw new UnsupportedOperationException("Order update not implemented yet.");
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable Long orderId) {
        // This method is a placeholder for deleting an order.
        // The actual implementation would involve deleting the order from the database
        // and publishing an event to RabbitMQ.
        throw new UnsupportedOperationException("Order deletion not implemented yet.");
    }

}
