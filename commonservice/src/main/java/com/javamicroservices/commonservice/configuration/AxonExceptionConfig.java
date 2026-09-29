package com.javamicroservices.commonservice.configuration;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.modelling.command.AggregateNotFoundException;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.queryhandling.QueryExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import com.javamicroservices.commonservice.exception.AppException;
import com.javamicroservices.commonservice.exception.ErrorDetail;

/**
 * Exception ném ra trong command/query handler khi đi qua Axon Server sẽ mất class gốc,
 * chỉ còn message và "details". Interceptor này gói AppException (và AggregateNotFoundException)
 * vào CommandExecutionException / QueryExecutionException kèm ErrorDetail chứa status code,
 * để ExceptionAdvice ở phía controller trả về đúng HTTP status thay vì 500.
 */
@Configuration
public class AxonExceptionConfig {

    @Autowired
    public void registerCommandInterceptor(CommandBus commandBus) {
        commandBus.registerHandlerInterceptor((unitOfWork, interceptorChain) -> {
            try {
                return interceptorChain.proceed();
            } catch (AppException ex) {
                throw new CommandExecutionException(ex.getMessage(), ex, new ErrorDetail(ex.getStatus().value(), ex.getMessage()));
            } catch (AggregateNotFoundException ex) {
                String message = "Resource not found with id: " + ex.getAggregateIdentifier();
                throw new CommandExecutionException(message, ex, new ErrorDetail(HttpStatus.NOT_FOUND.value(), message));
            }
        });
    }

    @Autowired
    public void registerQueryInterceptor(QueryBus queryBus) {
        queryBus.registerHandlerInterceptor((unitOfWork, interceptorChain) -> {
            try {
                return interceptorChain.proceed();
            } catch (AppException ex) {
                throw new QueryExecutionException(ex.getMessage(), ex, new ErrorDetail(ex.getStatus().value(), ex.getMessage()));
            }
        });
    }
}
