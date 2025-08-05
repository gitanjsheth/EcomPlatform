package com.gitanjsheth.ordermanagementservice.exceptions;

import com.gitanjsheth.ordermanagementservice.models.OrderStatus;

public class InvalidOrderStateException extends RuntimeException {
    
    private final OrderStatus currentStatus;
    private final OrderStatus targetStatus;
    private final String operation;
    
    public InvalidOrderStateException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
        this.operation = null;
    }
    
    public InvalidOrderStateException(String operation, OrderStatus currentStatus) {
        super(String.format("Cannot perform operation '%s' on order with status '%s'", operation, currentStatus));
        this.operation = operation;
        this.currentStatus = currentStatus;
        this.targetStatus = null;
    }
    
    public InvalidOrderStateException(String operation, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(String.format("Cannot change order status from '%s' to '%s' for operation '%s'", 
            currentStatus, targetStatus, operation));
        this.operation = operation;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public InvalidOrderStateException(String message, String operation, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(message);
        this.operation = operation;
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }
    
    public OrderStatus getCurrentStatus() {
        return currentStatus;
    }
    
    public OrderStatus getTargetStatus() {
        return targetStatus;
    }
    
    public String getOperation() {
        return operation;
    }
}