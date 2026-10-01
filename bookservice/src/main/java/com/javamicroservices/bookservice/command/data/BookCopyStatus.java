package com.javamicroservices.bookservice.command.data;

/**
 * AVAILABLE -> RESERVED -> BORROWED -> AVAILABLE (trả sách)
 * RESERVED -> AVAILABLE (compensation / timeout)
 * AVAILABLE -> LOST | DAMAGED
 */
public enum BookCopyStatus {
    AVAILABLE,
    RESERVED,
    BORROWED,
    LOST,
    DAMAGED
}
