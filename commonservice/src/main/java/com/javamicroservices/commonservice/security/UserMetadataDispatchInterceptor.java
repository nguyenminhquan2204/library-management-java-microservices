package com.javamicroservices.commonservice.security;

import java.util.HashMap;
import java.util.Map;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

/**
 * Gắn thông tin người gọi (userId, username) vào MetaData của command gửi từ REST controller.
 * Command do saga gửi (không có SecurityContext) giữ nguyên metadata được lan truyền từ event.
 */
@Component
@ConditionalOnClass(name = "org.springframework.security.oauth2.jwt.Jwt")
public class UserMetadataDispatchInterceptor {

    @Autowired
    public void register(CommandGateway commandGateway) {
        commandGateway.registerDispatchInterceptor(messages -> (index, message) -> {
            Map<String, String> userMetadata = new HashMap<>();
            String userId = SecurityUtils.currentUserId();
            String username = SecurityUtils.currentUsername();
            if (userId != null) userMetadata.put(AxonSecurityConfig.USER_ID, userId);
            if (username != null) userMetadata.put(AxonSecurityConfig.USERNAME, username);
            return userMetadata.isEmpty() ? message : message.andMetaData(userMetadata);
        });
    }
}
