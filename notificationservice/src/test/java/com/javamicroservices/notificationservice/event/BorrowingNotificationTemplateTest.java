package com.javamicroservices.notificationservice.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import com.javamicroservices.commonservice.model.BorrowingNotificationMessage;

import freemarker.template.Configuration;
import tools.jackson.databind.json.JsonMapper;

class BorrowingNotificationTemplateTest {
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    private BorrowingNotificationMessage overdueMessage() {
        return BorrowingNotificationMessage.builder()
            .type(BorrowingNotificationMessage.Type.OVERDUE)
            .borrowingId("b1")
            .recipientEmail("member@example.com")
            .employeeName("Nguyen Van A")
            .bookId("book-1")
            .bookName("Clean Architecture")
            .borrowingDate("01/09/2026")
            .dueDate("15/09/2026")
            .overdueDays(3)
            .fineAmount(BigDecimal.valueOf(15000))
            .finePerDay(BigDecimal.valueOf(5000))
            .currency("VND")
            .build();
    }

    private String render(String template, BorrowingNotificationMessage message) throws Exception {
        Configuration config = new Configuration(Configuration.VERSION_2_3_32);
        config.setClassForTemplateLoading(getClass(), "/templates");
        config.setDefaultEncoding("UTF-8");

        Map<String, Object> placeholders = new HashMap<>();
        placeholders.put("employeeName", message.getEmployeeName());
        placeholders.put("bookName", message.getBookName());
        placeholders.put("borrowingDate", message.getBorrowingDate());
        placeholders.put("dueDate", message.getDueDate());
        placeholders.put("overdueDays", message.getOverdueDays());
        placeholders.put("fineAmount", message.getFineAmount());
        placeholders.put("finePerDay", message.getFinePerDay());
        placeholders.put("currency", message.getCurrency());
        return FreeMarkerTemplateUtils.processTemplateIntoString(config.getTemplate(template), placeholders);
    }

    @Test
    void messageSurvivesJsonRoundTrip() {
        BorrowingNotificationMessage message = overdueMessage();
        String json = jsonMapper.writeValueAsString(message);

        assertEquals(message, jsonMapper.readValue(json, BorrowingNotificationMessage.class));
    }

    @Test
    void rendersOverdueEmail() throws Exception {
        String html = render("borrowingOverdue.ftl", overdueMessage());

        assertTrue(html.contains("Clean Architecture"));
        assertTrue(html.contains("15/09/2026"));
        assertTrue(html.contains("3 ngày"));
        assertTrue(html.matches("(?s).*15[.,]000 VND.*"), html);
    }

    @Test
    void rendersDueSoonEmail() throws Exception {
        BorrowingNotificationMessage message = overdueMessage();
        message.setType(BorrowingNotificationMessage.Type.DUE_SOON);
        message.setOverdueDays(0);
        message.setFineAmount(null);

        String html = render("borrowingDueSoon.ftl", message);

        assertTrue(html.contains("Nguyen Van A"));
        assertTrue(html.matches("(?s).*5[.,]000 VND.*"), html);
    }
}
