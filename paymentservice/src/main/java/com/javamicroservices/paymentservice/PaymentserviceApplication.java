package com.javamicroservices.paymentservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

import com.javamicroservices.commonservice.advise.ExceptionAdvice;
import com.javamicroservices.commonservice.configuration.AxonConfig;
import com.javamicroservices.commonservice.configuration.AxonExceptionConfig;
import com.javamicroservices.commonservice.security.AxonSecurityConfig;
import com.javamicroservices.commonservice.security.ResourceServerSecurityConfig;
import com.javamicroservices.commonservice.security.SecurityExceptionAdvice;
import com.javamicroservices.commonservice.security.UserMetadataDispatchInterceptor;

@SpringBootApplication
@EnableDiscoveryClient
@Import({
	AxonConfig.class,
	ExceptionAdvice.class,
	AxonExceptionConfig.class,
	ResourceServerSecurityConfig.class,
	SecurityExceptionAdvice.class,
	AxonSecurityConfig.class,
	UserMetadataDispatchInterceptor.class
})
public class PaymentserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymentserviceApplication.class, args);
	}

}
