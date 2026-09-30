package com.javamicroservices.notificationservice.event;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.BackOff;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.javamicroservices.commonservice.model.BorrowingNotificationMessage;
import com.javamicroservices.commonservice.services.EmailService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Nhận thông báo từ cronjob của borrowingservice và gửi email nhắc hạn trả / báo quá hạn kèm tiền phạt.
 */
@Component
@Slf4j
public class BorrowingNotificationConsumer {
    @Autowired
    private EmailService emailService;

    @Autowired
    private JsonMapper jsonMapper;

    @RetryableTopic(
        attempts = "4", // 3 topic retry + 1 topic DLQ
        backOff = @BackOff(delay = 1000, multiplier = 2),
        autoCreateTopics = "true",
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        exclude = {JacksonException.class} // Message sai format thì retry cũng vô ích -> vào thẳng DLT
    )
    @KafkaListener(topics = BorrowingNotificationMessage.TOPIC, containerFactory = "kafkaListenerContainerFactory")
    public void listen(String payload) {
        BorrowingNotificationMessage message = jsonMapper.readValue(payload, BorrowingNotificationMessage.class);
        log.info("Received {} notification for borrowing {}", message.getType(), message.getBorrowingId());

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("employeeName", message.getEmployeeName());
        placeholders.put("bookName", message.getBookName());
        placeholders.put("borrowingDate", message.getBorrowingDate());
        placeholders.put("dueDate", message.getDueDate());
        placeholders.put("overdueDays", message.getOverdueDays());
        placeholders.put("fineAmount", message.getFineAmount());
        placeholders.put("finePerDay", message.getFinePerDay());
        placeholders.put("currency", message.getCurrency());

        switch (message.getType()) {
            case DUE_SOON -> emailService.sendEmailWithTemplate(
                message.getRecipientEmail(),
                "Nhắc hạn trả sách: " + message.getBookName(),
                "borrowingDueSoon.ftl",
                placeholders,
                null);
            case OVERDUE -> emailService.sendEmailWithTemplate(
                message.getRecipientEmail(),
                "Sách quá hạn " + message.getOverdueDays() + " ngày - tiền phạt tạm tính",
                "borrowingOverdue.ftl",
                placeholders,
                null);
        }
    }

    @DltHandler
    public void processDltMessage(@Payload String payload) {
        log.error("Borrowing notification moved to DLT: {}", payload);
    }
}
