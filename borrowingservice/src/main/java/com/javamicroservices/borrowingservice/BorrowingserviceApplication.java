package com.javamicroservices.borrowingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

import com.javamicroservices.commonservice.advise.ExceptionAdvice;
import com.javamicroservices.commonservice.configuration.AxonExceptionConfig;

@SpringBootApplication
@Import({ ExceptionAdvice.class, AxonExceptionConfig.class })
public class BorrowingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BorrowingserviceApplication.class, args);
	}

}
