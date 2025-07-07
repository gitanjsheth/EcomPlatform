package com.gitanjsheth.productservice.exceptions;

import lombok.Getter;

@Getter
public class CategoryNotFoundException extends Exception {
    private Long categoryId;
    
    public CategoryNotFoundException(String message) {
        super(message);
    }
    
    public CategoryNotFoundException(String message, Long categoryId) {
        super(message);
        this.categoryId = categoryId;
    }
}
