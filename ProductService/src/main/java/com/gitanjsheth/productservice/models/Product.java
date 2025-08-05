package com.gitanjsheth.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Entity
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE product SET deleted = true WHERE id = ?")
public class Product extends BaseModel {
    @Column(nullable = false)
    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 100, message = "Product title must be between 3 and 100 characters")
    private String title;
    
    @Column(nullable = false)
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Integer price;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private String imageURL;
    
    @ManyToOne
    @NotNull(message = "Category is required")
    private Category category;
    
    // Inventory Management Fields
    @Column(name = "stock_quantity", nullable = false)
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity = 0;
    
    @Column(name = "reserved_quantity", nullable = false)
    @Min(value = 0, message = "Reserved quantity cannot be negative")
    private Integer reservedQuantity = 0; // Temporarily held for pending orders
    
    @Column(name = "available_quantity", nullable = false)
    @Min(value = 0, message = "Available quantity cannot be negative")
    private Integer availableQuantity = 0; // stockQuantity - reservedQuantity
    
    @Column(name = "is_out_of_stock", nullable = false)
    private Boolean isOutOfStock = false;
    
    @Column(name = "show_when_out_of_stock", nullable = false)
    private Boolean showWhenOutOfStock = true; // Show with OOS status
    
    @Column(name = "allow_backorder", nullable = false)
    private Boolean allowBackorder = false; // Allow ordering when OOS
    
    @Column(name = "low_stock_threshold")
    @Min(value = 0, message = "Low stock threshold cannot be negative")
    private Integer lowStockThreshold = 10;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // Product visibility toggle
    
    // Auto-calculated field
    public Integer getAvailableQuantity() {
        return stockQuantity - reservedQuantity;
    }
    
    // Auto-update out of stock status
    public void updateOutOfStockStatus() {
        this.isOutOfStock = getAvailableQuantity() <= 0;
    }
    
    // Check if product can be added to cart
    public boolean isAvailableForCart(Integer requestedQuantity) {
        return isActive && 
               (!isOutOfStock || allowBackorder) && 
               (allowBackorder || getAvailableQuantity() >= requestedQuantity);
    }
}
