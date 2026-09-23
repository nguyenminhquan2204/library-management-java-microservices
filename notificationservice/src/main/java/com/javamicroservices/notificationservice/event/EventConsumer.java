package com.javamicroservices.notificationservice.event;

import org.apache.kafka.common.errors.RetriableException;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class EventConsumer {

    @RetryableTopic(
        attempts = "4", // 3 topic retry + 1 topic DLQ
        backOff = @BackOff(delay = 1000, multiplier = 2),
        autoCreateTopics = "true",
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        include = {RetriableException.class, RuntimeException.class} // Allow exception can retry
    )
    @KafkaListener(topics = "test", containerFactory = "kafkaListenerContainerFactory")
    public void listen(String message) {
        log.info("Received message: " + message);
        
        // processing message
        throw new RuntimeException("Error test");
    }

    @DltHandler
    public void processDltMessage(@Payload String message) {
        log.info("DLT receive message: " + message);
    }
}
