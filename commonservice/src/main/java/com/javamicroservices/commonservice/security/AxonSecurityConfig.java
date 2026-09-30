package com.javamicroservices.commonservice.security;

import org.axonframework.messaging.correlation.CorrelationDataProvider;
import org.axonframework.messaging.correlation.MessageOriginProvider;
import org.axonframework.messaging.correlation.SimpleCorrelationDataProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Lan truyền userId/username từ MetaData của command sang event và các command do saga sinh ra -> phục vụ audit.
 * (Việc gắn metadata vào command nằm ở UserMetadataDispatchInterceptor; tách riêng để tránh vòng phụ thuộc
 * CorrelationDataProvider -> CommandGateway khi Axon khởi tạo.)
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.security.oauth2.jwt.Jwt")
public class AxonSecurityConfig {

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";

    /** Khai báo lại provider mặc định của Axon (bị tắt khi có CorrelationDataProvider bean khác). */
    @Bean
    public CorrelationDataProvider messageOriginCorrelationDataProvider() {
        return new MessageOriginProvider();
    }

    @Bean
    public CorrelationDataProvider userCorrelationDataProvider() {
        return new SimpleCorrelationDataProvider(USER_ID, USERNAME);
    }
}
