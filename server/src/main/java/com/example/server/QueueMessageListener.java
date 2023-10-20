package com.example.server;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class QueueMessageListener {

    @RabbitListener(queues = "test_queue")
    public void onMessageFromQueue(Object message) {
        System.out.println("queue test_queue received message: " + message);
    }
}
