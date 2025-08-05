package com.gitanjsheth.ordermanagementservice.dtos;

import com.gitanjsheth.ordermanagementservice.models.OrderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderTrackingDto {
    private String orderNumber;
    private OrderStatus status;
    private String trackingNumber;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private DeliveryAddressDto deliveryAddress;
    private List<OrderTrackingEvent> trackingEvents;
    private String currentLocation;
    private String estimatedDeliveryTime;
    
    @Getter
    @Setter
    public static class OrderTrackingEvent {
        private String status;
        private String description;
        private String location;
        private LocalDateTime timestamp;
        
        public OrderTrackingEvent() {}
        
        public OrderTrackingEvent(String status, String description, String location, LocalDateTime timestamp) {
            this.status = status;
            this.description = description;
            this.location = location;
            this.timestamp = timestamp;
        }
    }
}