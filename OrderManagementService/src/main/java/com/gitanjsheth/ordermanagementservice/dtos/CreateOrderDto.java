package com.gitanjsheth.ordermanagementservice.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderDto {
    
    @NotBlank(message = "Cart ID is required")
    private String cartId;
    
    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDto deliveryAddress;
    
    private String notes;
    
    // Payment method preference (optional)
    private String preferredPaymentMethod;
}