package com.gitanjsheth.ordermanagementservice.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {
    private String orderId;
    private String eventType; // RESERVE, RELEASE, CONFIRM
    private List<InventoryItem> items;
    private LocalDateTime timestamp;
    
    public InventoryEvent(String orderId, String eventType, List<InventoryItem> items) {
        this.orderId = orderId;
        this.eventType = eventType;
        this.items = items;
        this.timestamp = LocalDateTime.now();
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InventoryItem {
        private Long productId;
        private Integer quantity;
        private String action; // RESERVE, RELEASE, CONFIRM
    }
}