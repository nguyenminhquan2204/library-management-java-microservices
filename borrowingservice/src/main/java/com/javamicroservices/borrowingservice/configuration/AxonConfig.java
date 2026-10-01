package com.javamicroservices.borrowingservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.serialization.xml.CompactDriver;

import com.thoughtworks.xstream.XStream;

@Configuration 
public class AxonConfig {

    @Bean 
    public XStream xStream() {
        XStream xStream = new XStream(new CompactDriver());
        xStream.allowTypesByWildcard(new String[] { "com.javamicroservices.**" });
        return xStream;
    }

    /**
     * Dùng cho timeout của BorrowingSaga. SimpleDeadlineManager lưu lịch trong bộ nhớ:
     * restart service thì các deadline đang chờ bị mất.
     */
    @Bean
    public DeadlineManager deadlineManager(org.axonframework.config.Configuration configuration, TransactionManager transactionManager) {
        return SimpleDeadlineManager.builder()
            .scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
            .transactionManager(transactionManager)
            .build();
    }
}
