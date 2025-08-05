package com.gitanjsheth.ordermanagementservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class DeliveryAddress {
    
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name cannot exceed 100 characters")
    @Column(name = "delivery_full_name")
    private String fullName;
    
    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "delivery_phone")
    private String phone;
    
    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255, message = "Address line 1 cannot exceed 255 characters")
    @Column(name = "delivery_address_line1")
    private String addressLine1;
    
    @Size(max = 255, message = "Address line 2 cannot exceed 255 characters")
    @Column(name = "delivery_address_line2")
    private String addressLine2;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Column(name = "delivery_city")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    @Column(name = "delivery_state")
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    @Size(max = 20, message = "ZIP code cannot exceed 20 characters")
    @Column(name = "delivery_zip_code")
    private String zipCode;
    
    @NotBlank(message = "Country is required")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Column(name = "delivery_country")
    private String country;
    
    @Size(max = 500, message = "Instructions cannot exceed 500 characters")
    @Column(name = "delivery_instructions")
    private String instructions;
    
    // Default constructor
    public DeliveryAddress() {}
    
    // Constructor with required fields
    public DeliveryAddress(String fullName, String phone, String addressLine1, 
                          String city, String state, String zipCode, String country) {
        this.fullName = fullName;
        this.phone = phone;
        this.addressLine1 = addressLine1;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }
    
    // Business methods
    
    public String getFormattedAddress() {
        StringBuilder address = new StringBuilder();
        address.append(addressLine1);
        
        if (addressLine2 != null && !addressLine2.trim().isEmpty()) {
            address.append(", ").append(addressLine2);
        }
        
        address.append(", ").append(city)
               .append(", ").append(state)
               .append(" ").append(zipCode)
               .append(", ").append(country);
        
        return address.toString();
    }
}