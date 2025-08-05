package com.gitanjsheth.ordermanagementservice.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE orders SET deleted = true WHERE id = ?")
public class Order extends BaseModel {
    
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber; // ORD-20241225-001
    
    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "cart_id")
    private String cartId; // Reference to original cart
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();
    
    @Embedded
    private DeliveryAddress deliveryAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.CREATED;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Positive(message = "Total amount must be positive")
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;
    
    @Column(name = "shipping_amount", precision = 19, scale = 2)
    private BigDecimal shippingAmount = BigDecimal.ZERO;
    
    @Column(name = "discount_amount", precision = 19, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();
    
    @Column(name = "expected_delivery_date")
    private LocalDateTime expectedDeliveryDate;
    
    @Column(name = "actual_delivery_date")
    private LocalDateTime actualDeliveryDate;
    
    @Column(name = "tracking_number")
    private String trackingNumber;
    
    @Column(name = "payment_id")
    private String paymentId; // From payment service
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "inventory_reserved", nullable = false)
    private Boolean inventoryReserved = false;
    
    @Column(name = "inventory_reserved_at")
    private LocalDateTime inventoryReservedAt;
    
    @Column(name = "inventory_reservation_expires_at")
    private LocalDateTime inventoryReservationExpiresAt;
    
    // Business methods
    
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
        recalculateTotal();
    }
    
    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
        recalculateTotal();
    }
    
    public void recalculateTotal() {
        BigDecimal itemsTotal = items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        this.totalAmount = itemsTotal
            .add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
            .add(shippingAmount != null ? shippingAmount : BigDecimal.ZERO)
            .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }
    
    public int getTotalItems() {
        return items.size();
    }
    
    public int getTotalQuantity() {
        return items.stream()
            .mapToInt(OrderItem::getQuantity)
            .sum();
    }
    
    public boolean isInventoryReservationExpired() {
        return inventoryReservationExpiresAt != null && 
               LocalDateTime.now().isAfter(inventoryReservationExpiresAt);
    }
    
    public boolean canBeCancelled() {
        return status == OrderStatus.CREATED || 
               status == OrderStatus.PAYMENT_PENDING || 
               status == OrderStatus.CONFIRMED;
    }
    
    public boolean canBeRefunded() {
        return status == OrderStatus.CONFIRMED || 
               status == OrderStatus.PROCESSING || 
               status == OrderStatus.SHIPPED;
    }
    
    public void markInventoryAsReserved() {
        this.inventoryReserved = true;
        this.inventoryReservedAt = LocalDateTime.now();
        this.inventoryReservationExpiresAt = LocalDateTime.now().plusHours(24);
    }
    
    public void markInventoryAsReleased() {
        this.inventoryReserved = false;
        this.inventoryReservedAt = null;
        this.inventoryReservationExpiresAt = null;
    }
}