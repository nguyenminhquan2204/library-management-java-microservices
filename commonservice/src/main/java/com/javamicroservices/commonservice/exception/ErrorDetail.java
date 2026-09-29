package com.javamicroservices.commonservice.exception;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Thông tin lỗi được gửi kèm CommandExecutionException / QueryExecutionException
 * qua Axon Server, để phía gọi biết được HTTP status gốc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetail implements Serializable {

    private int statusCode;

    private String message;
}
