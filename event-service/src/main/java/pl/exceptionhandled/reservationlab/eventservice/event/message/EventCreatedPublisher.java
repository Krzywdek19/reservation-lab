package pl.exceptionhandled.reservationlab.eventservice.event.message;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventCreatedPublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange}")
    private String exchange;

    @Value("${app.messaging.routing-keys.event-created}")
    private String eventCreatedRoutingKey;

    public void publish(EventCreatedMessage message) {
        rabbitTemplate.convertAndSend(exchange, eventCreatedRoutingKey, message);
    }
}
