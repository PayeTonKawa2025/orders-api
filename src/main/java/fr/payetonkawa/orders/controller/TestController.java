package fr.payetonkawa.orders.controller;

import fr.payetonkawa.orders.event.EventPublisher;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
public class TestController {

    private final EventPublisher eventPublisher;

    @GetMapping
    public void test() {
        eventPublisher.sendEvent("order.test", "Ceci est un message");
    }

}
