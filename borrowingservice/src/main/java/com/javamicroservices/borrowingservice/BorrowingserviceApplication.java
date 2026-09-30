package com.javamicroservices.borrowingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.javamicroservices.commonservice.advise.ExceptionAdvice;
import com.javamicroservices.commonservice.configuration.AxonExceptionConfig;
import com.javamicroservices.commonservice.configuration.KafkaConfig;
import com.javamicroservices.commonservice.security.AxonSecurityConfig;
import com.javamicroservices.commonservice.security.ResourceServerSecurityConfig;
import com.javamicroservices.commonservice.security.SecurityExceptionAdvice;
import com.javamicroservices.commonservice.security.UserMetadataDispatchInterceptor;
import com.javamicroservices.commonservice.services.KafkaService;

@SpringBootApplication
@EnableScheduling
@Import({
	ExceptionAdvice.class,
	AxonExceptionConfig.class,
	ResourceServerSecurityConfig.class,
	SecurityExceptionAdvice.class,
	AxonSecurityConfig.class,
	UserMetadataDispatchInterceptor.class,
	KafkaConfig.class,
	KafkaService.class
})
public class BorrowingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BorrowingserviceApplication.class, args);
	}

}
