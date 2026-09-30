package com.javamicroservices.borrowingservice.scheduler;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.javamicroservices.borrowingservice.command.command.NotifyBorrowingDueSoonCommand;
import com.javamicroservices.borrowingservice.command.command.RecordBorrowingOverdueCommand;
import com.javamicroservices.borrowingservice.command.data.Borrowing;
import com.javamicroservices.borrowingservice.command.data.BorrowingRepository;
import com.javamicroservices.borrowingservice.command.event.BorrowingOverdueRecordedEvent;
import com.javamicroservices.borrowingservice.configuration.BorrowingPolicy;
import com.javamicroservices.commonservice.model.BookResponseCommonModel;
import com.javamicroservices.commonservice.model.BorrowingNotificationMessage;
import com.javamicroservices.commonservice.model.EmployeeResponseCommonModel;
import com.javamicroservices.commonservice.queries.GetBookDetailQuery;
import com.javamicroservices.commonservice.queries.GetDetailEmployeeQuery;
import com.javamicroservices.commonservice.services.KafkaService;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

/**
 * Cronjob quét phiếu mượn chưa trả:
 * - Sắp đến hạn (trong {@code borrowing.policy.reminder-days-before} ngày tới) -> nhắc 1 lần.
 * - Đã quá hạn -> ghi nhận tiền phạt tạm tính và báo mỗi ngày 1 lần.
 *
 * Mỗi phiếu: gửi command vào aggregate trước (aggregate quyết định có được gửi hay không, lưu lại bằng event),
 * thành công mới publish message lên Kafka cho notificationservice gửi email.
 */
@Slf4j
@Component
public class BorrowingReminderJob {
    private static final String DATE_PATTERN = "dd/MM/yyyy";

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private CommandGateway commandGateway;

    @Autowired
    private QueryGateway queryGateway;

    @Autowired
    private KafkaService kafkaService;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private BorrowingPolicy policy;

    @Scheduled(cron = "${borrowing.reminder.cron:0 0 8 * * *}")
    public void scheduledRun() {
        run();
    }

    /** synchronized: tránh chạy chồng khi vừa tới lịch vừa được ADMIN trigger bằng tay. */
    public synchronized ReminderJobResult run() {
        Date now = new Date();
        Date startOfToday = policy.startOfDay(now, 0);
        Date reminderWindowEnd = policy.startOfDay(now, policy.getReminderDaysBefore() + 1);

        List<Borrowing> dueSoon = borrowingRepository.findDueSoon(startOfToday, reminderWindowEnd);
        List<Borrowing> overdue = borrowingRepository.findOverdueNotNotifiedToday(startOfToday);
        log.info("Borrowing reminder job s. tarted: {} due soon, {} overdue", dueSoon.size(), overdue.size());

        ReminderJobResult result = new ReminderJobResult();
        dueSoon.forEach(borrowing -> {
            if (notifyDueSoon(borrowing, now)) result.setDueSoonSent(result.getDueSoonSent() + 1);
            else result.setSkipped(result.getSkipped() + 1);
        });
        overdue.forEach(borrowing -> {
            if (notifyOverdue(borrowing, now)) result.setOverdueSent(result.getOverdueSent() + 1);
            else result.setSkipped(result.getSkipped() + 1);
        });

        log.info("Borrowing reminder job finished: {}", result);
        return result;
    }

    private boolean notifyDueSoon(Borrowing borrowing, Date now) {
        try {
            EmployeeResponseCommonModel employee = getEmployee(borrowing.getEmployeeId());
            if (!hasEmail(employee)) {
                log.warn("Skip due soon reminder for borrowing {}: employee {} has no email", borrowing.getId(), borrowing.getEmployeeId());
                return false;
            }

            commandGateway.sendAndWait(new NotifyBorrowingDueSoonCommand(borrowing.getId(), now));
            publish(baseMessage(BorrowingNotificationMessage.Type.DUE_SOON, borrowing, employee)
                .fineAmount(BigDecimal.ZERO)
                .build());
            return true;
        } catch (Exception e) {
            log.warn("Skip due soon reminder for borrowing {}: {}", borrowing.getId(), e.getMessage());
            return false;
        }
    }

    private boolean notifyOverdue(Borrowing borrowing, Date now) {
        try {
            EmployeeResponseCommonModel employee = getEmployee(borrowing.getEmployeeId());
            if (!hasEmail(employee)) {
                log.warn("Skip overdue notice for borrowing {}: employee {} has no email", borrowing.getId(), borrowing.getEmployeeId());
                return false;
            }

            BorrowingOverdueRecordedEvent recorded = commandGateway.sendAndWait(new RecordBorrowingOverdueCommand(borrowing.getId(), now));
            publish(baseMessage(BorrowingNotificationMessage.Type.OVERDUE, borrowing, employee)
                .overdueDays(recorded.getOverdueDays())
                .fineAmount(recorded.getFineAmount())
                .build());
            return true;
        } catch (Exception e) {
            log.warn("Skip overdue notice for borrowing {}: {}", borrowing.getId(), e.getMessage());
            return false;
        }
    }

    private BorrowingNotificationMessage.BorrowingNotificationMessageBuilder baseMessage(
            BorrowingNotificationMessage.Type type, Borrowing borrowing, EmployeeResponseCommonModel employee) {
        return BorrowingNotificationMessage.builder()
            .type(type)
            .borrowingId(borrowing.getId())
            .recipientEmail(employee.getEmail())
            .employeeName(fullName(employee))
            .bookId(borrowing.getBookId())
            .bookName(getBookName(borrowing.getBookId()))
            .borrowingDate(format(borrowing.getBorrowingDate()))
            .dueDate(format(borrowing.getDueDate()))
            .finePerDay(policy.getFinePerDay())
            .currency(policy.getCurrency());
    }

    private void publish(BorrowingNotificationMessage message) {
        kafkaService.sendMessage(BorrowingNotificationMessage.TOPIC, jsonMapper.writeValueAsString(message));
    }

    private EmployeeResponseCommonModel getEmployee(String employeeId) {
        GetDetailEmployeeQuery query = new GetDetailEmployeeQuery(employeeId);
        return queryGateway.query(query, ResponseTypes.instanceOf(EmployeeResponseCommonModel.class)).join();
    }

    private String getBookName(String bookId) {
        try {
            GetBookDetailQuery query = new GetBookDetailQuery(bookId);
            BookResponseCommonModel book = queryGateway.query(query, ResponseTypes.instanceOf(BookResponseCommonModel.class)).join();
            return book.getName();
        } catch (Exception e) {
            log.warn("Cannot get book detail for bookId {}: {}", bookId, e.getMessage());
            return bookId;
        }
    }

    private boolean hasEmail(EmployeeResponseCommonModel employee) {
        return employee != null && employee.getEmail() != null && !employee.getEmail().isBlank();
    }

    private String fullName(EmployeeResponseCommonModel employee) {
        String firstName = employee.getFirstName() != null ? employee.getFirstName() : "";
        String lastName = employee.getLastName() != null ? employee.getLastName() : "";
        return (firstName + " " + lastName).trim();
    }

    private String format(Date date) {
        if (date == null) {
            return null;
        }
        return DateTimeFormatter.ofPattern(DATE_PATTERN).withZone(policy.getZone()).format(date.toInstant());
    }
}
