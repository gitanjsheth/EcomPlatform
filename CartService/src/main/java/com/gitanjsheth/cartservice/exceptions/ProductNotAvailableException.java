package com.gitanjsheth.cartservice.exceptions;

public class ProductNotAvailableException extends RuntimeException {
    public ProductNotAvailableException(String message) {
        super(message);
    }
    
    public ProductNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}