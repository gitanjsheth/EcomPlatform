package com.gitanjsheth.ordermanagementservice.exceptions;

public class InsufficientInventoryException extends RuntimeException {
    
    private final Long productId;
    private final Integer requestedQuantity;
    private final Integer availableQuantity;
    
    public InsufficientInventoryException(String message) {
        super(message);
        this.productId = null;
        this.requestedQuantity = null;
        this.availableQuantity = null;
    }
    
    public InsufficientInventoryException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient inventory for product ID %d. Requested: %d, Available: %d", 
            productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientInventoryException(String message, Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(message);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}