package com.gitanjsheth.productservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductAvailabilityDto {
    
    private Long productId;
    private String title;
    private Boolean isActive;
    private Boolean isOutOfStock;
    private Boolean allowBackorder;
    private Integer stockQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private Integer lowStockThreshold;
    private Boolean showWhenOutOfStock;
    
    public ProductAvailabilityDto() {}
    
    public ProductAvailabilityDto(Long productId, String title, Boolean isActive, 
                                Boolean isOutOfStock, Boolean allowBackorder,
                                Integer stockQuantity, Integer reservedQuantity, 
                                Integer availableQuantity, Integer lowStockThreshold,
                                Boolean showWhenOutOfStock) {
        this.productId = productId;
        this.title = title;
        this.isActive = isActive;
        this.isOutOfStock = isOutOfStock;
        this.allowBackorder = allowBackorder;
        this.stockQuantity = stockQuantity;
        this.reservedQuantity = reservedQuantity;
        this.availableQuantity = availableQuantity;
        this.lowStockThreshold = lowStockThreshold;
        this.showWhenOutOfStock = showWhenOutOfStock;
    }
}