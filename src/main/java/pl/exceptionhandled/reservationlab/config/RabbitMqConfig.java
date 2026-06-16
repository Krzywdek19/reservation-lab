package pl.exceptionhandled.reservationlab.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    public Queue eventCreatedQueue(
            @Value("${app.messaging.queues.event-created}") String queueName
    ) {
        return QueueBuilder.durable(queueName).build();
    }
}
