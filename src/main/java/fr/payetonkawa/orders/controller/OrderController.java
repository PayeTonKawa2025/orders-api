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
    public OrderDto createOrder(@RequestBody OrderDto orderDto) {
        return orderService.create(orderDto);
    }

    @PatchMapping("/{orderId}")
    public OrderDto updateOrder(@PathVariable Long orderId, @RequestBody OrderDto orderDto) {
        return orderService.update(orderId, orderDto);
    }

    @DeleteMapping("/{orderId}")
    public void deleteOrder(@PathVariable Long orderId) {
        orderService.delete(orderId);
    }

}
