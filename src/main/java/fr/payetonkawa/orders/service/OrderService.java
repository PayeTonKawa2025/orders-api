package fr.payetonkawa.orders.service;

import fr.payetonkawa.orders.dto.OrderDto;
import fr.payetonkawa.orders.entity.Order;
import fr.payetonkawa.orders.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public List<OrderDto> getByClientId(String clientId) {
        List<Order> orders = orderRepository.findAllByClientId(clientId);
        return OrderDto.fromEntities(orders);
    }

}
