package com.javamicroservices.notificationservice.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.errors.RetriableException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.javamicroservices.commonservice.services.EmailService;

import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class EventConsumer {

    @Autowired 
    private EmailService emailService;

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

    @KafkaListener(topics = "testEmail", containerFactory = "kafkaListenerContainerFactory")
    public void testEmail(String message) {
        log.info("Reveiced message: " + message);

        String template = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Thank You for Borrowing a Book</title>
            </head>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 30px; border-radius: 8px;">
                    
                    <h2 style="color: #333333;">Thank You for Borrowing a Book!</h2>

                    <p style="color: #555555; font-size: 16px;">
                        Dear %s,
                    </p>

                    <p style="color: #555555; font-size: 16px; line-height: 1.6;">
                        Thank you for borrowing a book from our library.
                        We hope you enjoy reading it and find it useful.
                    </p>

                    <p style="color: #555555; font-size: 16px; line-height: 1.6;">
                        Please remember to return the book by the due date.
                        We appreciate your cooperation in helping us keep our library
                        available for everyone.
                    </p>

                    <p style="color: #555555; font-size: 16px;">
                        Happy reading!
                    </p>

                    <hr style="border: none; border-top: 1px solid #eeeeee; margin: 30px 0;">

                    <p style="color: #888888; font-size: 14px;">
                        Best regards,<br>
                        Library Management Team
                    </p>

                </div>
            </body>
            </html>
        """;

        String filledTemplate = String.format(template, "Nguyen Quan");

        emailService.sendEmail(
                message,
                "Thank You for Borrowing a Book",
                filledTemplate,
                true,
                null
        );
    }

    @KafkaListener(topics = "emailTemplate", containerFactory = "kafkaListenerContainerFactory") 
    public void emailTemplate(String message) {
        log.info("Received message: " + message);

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("name", "Java Microservices");

        emailService.sendEmailWithTemplate(message,"Welcome to Christmas","emailTemplate.ftl",placeholders,null);
    }
}
