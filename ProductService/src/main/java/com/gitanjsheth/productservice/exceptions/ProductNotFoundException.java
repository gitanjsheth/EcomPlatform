package com.gitanjsheth.productservice.exceptions;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends Exception {
    private final long productId;

    public ProductNotFoundException(long productId) {
        this.productId = productId;
    }
}
