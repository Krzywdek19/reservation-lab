package pl.exceptionhandled.reservationlab.event.message.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pl.exceptionhandled.reservationlab.event.message.EventCreatedMessage;
import pl.exceptionhandled.reservationlab.event.service.EventSynchronizationService;

@Component
@RequiredArgsConstructor
public class EventCreatedMessageListener {
    private final EventSynchronizationService eventSynchronizationService;

    @RabbitListener(queues = "${app.messaging.queues.event-created}}")
    public void handle(EventCreatedMessage eventCreatedMessage) {
        eventSynchronizationService.synchronizeEventCreated(eventCreatedMessage);
    }
}
