package fr.payetonkawa.orders.controller;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@AllArgsConstructor
@Tag(name = "Orders", description = "Operations related to orders")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{clientId}")
    @Operation(summary = "Get orders by client ID",
               description = "Retrieve all orders associated with a specific client ID")
    public List<OrderDto> getOrdersByClientId(@PathVariable String clientId) {
        return orderService.getByClientId(clientId);
    }

    @PostMapping
    @Operation(summary = "Create a new order",
               description = "Create a new order with the provided details")
    public OrderDto createOrder(@RequestBody OrderDto orderDto) {
        return orderService.create(orderDto);
    }

    @PatchMapping("/{orderId}")
    @Operation(summary = "Update an existing order",
               description = "Update the details of an existing order by its ID")
    public OrderDto updateOrder(@PathVariable Long orderId, @RequestBody OrderDto orderDto) {
        return orderService.update(orderId, orderDto);
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete an order",
               description = "Delete an existing order by its ID")
    public void deleteOrder(@PathVariable Long orderId) {
        orderService.delete(orderId);
    }

}
