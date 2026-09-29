package com.javamicroservices.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Wrapper class cho tất cả API responses.
 * Đảm bảo format nhất quán cho cả success và error cases.
 *
 * @param <T> Kiểu dữ liệu của field data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    // ========== FIELDS ==========

    private int statusCode;
    private String message;

    // Chỉ có khi SUCCESS
    private T data;

    // Chỉ có khi ERROR
    private String error;

    // Chỉ có khi VALIDATION ERROR (nhiều lỗi)
    private List<String> details;

    private Instant timestamp;

    // ========== CONSTRUCTORS ==========

    public ApiResponse() {
    }

    // Constructor cho Success
    private ApiResponse(int statusCode, String message, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.data = data;
        // this.timestamp = Instant.now();
    }

    // Constructor cho Error
    private ApiResponse(int statusCode, String message, String error, List<String> details) {
        this.statusCode = statusCode;
        this.message = message;
        this.error = error;
        this.details = details;
        // this.timestamp = Instant.now();
    }

    // ========== SUCCESS METHODS ==========

    /**
     * Response thành công với data
     * HTTP 200 OK
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "Success", data);
    }

    /**
     * Response thành công với message tùy chỉnh
     * HTTP 200 OK
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    /**
     * Response tạo mới thành công
     * HTTP 201 Created
     */
    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Created successfully", data);
    }

    /**
     * Response tạo mới thành công với message tùy chỉnh
     * HTTP 201 Created
     */
    public static <T> ApiResponse<T> created(String message, T data) {
        return new ApiResponse<>(201, message, data);
    }

    // ========== ERROR METHODS ==========

    /**
     * Response lỗi không tìm thấy
     * HTTP 404 Not Found
     */
    public static <T> ApiResponse<T> notFound(String message) {
        return new ApiResponse<>(404, message, "Not Found", null);
    }

    /**
     * Response lỗi request không hợp lệ
     * HTTP 400 Bad Request
     */
    public static <T> ApiResponse<T> badRequest(String message) {
        return new ApiResponse<>(400, message, "Bad Request", null);
    }

    /**
     * Response lỗi validation với danh sách chi tiết
     * HTTP 400 Bad Request
     */
    public static <T> ApiResponse<T> badRequest(String message, List<String> details) {
        return new ApiResponse<>(400, message, "Bad Request", details);
    }

    /**
     * Response lỗi xung đột (duplicate, conflict)
     * HTTP 409 Conflict
     */
    public static <T> ApiResponse<T> conflict(String message) {
        return new ApiResponse<>(409, message, "Conflict", null);
    }

    /**
     * Response lỗi không có quyền truy cập
     * HTTP 401 Unauthorized
     */
    public static <T> ApiResponse<T> unauthorized(String message) {
        return new ApiResponse<>(401, message, "Unauthorized", null);
    }

    /**
     * Response lỗi không đủ quyền
     * HTTP 403 Forbidden
     */
    public static <T> ApiResponse<T> forbidden(String message) {
        return new ApiResponse<>(403, message, "Forbidden", null);
    }

    /**
     * Response lỗi server
     * HTTP 500 Internal Server Error
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, "Internal Server Error", null);
    }

    /**
     * Response với status code tùy chỉnh (success)
     */
    public static <T> ApiResponse<T> of(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, data);
    }

    /**
     * Response với status code tùy chỉnh (error)
     */
    public static <T> ApiResponse<T> ofError(int statusCode, String message, String error) {
        return new ApiResponse<>(statusCode, message, error, null);
    }

    // ========== GETTERS & SETTERS ==========

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}