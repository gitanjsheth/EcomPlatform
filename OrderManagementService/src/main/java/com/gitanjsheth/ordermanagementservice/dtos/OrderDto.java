package com.gitanjsheth.ordermanagementservice.dtos;

import com.gitanjsheth.ordermanagementservice.models.OrderStatus;
import com.gitanjsheth.ordermanagementservice.models.PaymentStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long userId;
    private String cartId;
    private List<OrderItemDto> items;
    private DeliveryAddressDto deliveryAddress;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal shippingAmount;
    private BigDecimal discountAmount;
    private LocalDateTime orderDate;
    private LocalDateTime expectedDeliveryDate;
    private LocalDateTime actualDeliveryDate;
    private String trackingNumber;
    private String paymentId;
    private String notes;
    private Boolean inventoryReserved;
    private LocalDateTime inventoryReservedAt;
    private LocalDateTime inventoryReservationExpiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}