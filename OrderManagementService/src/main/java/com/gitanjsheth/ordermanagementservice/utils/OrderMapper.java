package com.gitanjsheth.ordermanagementservice.utils;

import com.gitanjsheth.ordermanagementservice.dtos.DeliveryAddressDto;
import com.gitanjsheth.ordermanagementservice.dtos.OrderDto;
import com.gitanjsheth.ordermanagementservice.dtos.OrderItemDto;
import com.gitanjsheth.ordermanagementservice.models.DeliveryAddress;
import com.gitanjsheth.ordermanagementservice.models.Order;
import com.gitanjsheth.ordermanagementservice.models.OrderItem;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class OrderMapper {
    
    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }
        
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUserId());
        dto.setCartId(order.getCartId());
        dto.setStatus(order.getStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setTaxAmount(order.getTaxAmount());
        dto.setShippingAmount(order.getShippingAmount());
        dto.setDiscountAmount(order.getDiscountAmount());
        dto.setOrderDate(order.getOrderDate());
        dto.setExpectedDeliveryDate(order.getExpectedDeliveryDate());
        dto.setActualDeliveryDate(order.getActualDeliveryDate());
        dto.setTrackingNumber(order.getTrackingNumber());
        dto.setPaymentId(order.getPaymentId());
        dto.setNotes(order.getNotes());
        dto.setInventoryReserved(order.getInventoryReserved());
        dto.setInventoryReservedAt(order.getInventoryReservedAt());
        dto.setInventoryReservationExpiresAt(order.getInventoryReservationExpiresAt());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        
        // Map delivery address
        if (order.getDeliveryAddress() != null) {
            dto.setDeliveryAddress(toDto(order.getDeliveryAddress()));
        }
        
        // Map order items
        if (order.getItems() != null) {
            dto.setItems(order.getItems().stream()
                .map(this::toDto)
                .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    public OrderItemDto toDto(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }
        
        OrderItemDto dto = new OrderItemDto();
        dto.setId(orderItem.getId());
        dto.setProductId(orderItem.getProductId());
        dto.setProductTitle(orderItem.getProductTitle());
        dto.setProductImageUrl(orderItem.getProductImageUrl());
        dto.setUnitPrice(orderItem.getUnitPrice());
        dto.setQuantity(orderItem.getQuantity());
        dto.setSubtotal(orderItem.getSubtotal());
        dto.setCreatedAt(orderItem.getCreatedAt());
        
        return dto;
    }
    
    public DeliveryAddressDto toDto(DeliveryAddress address) {
        if (address == null) {
            return null;
        }
        
        DeliveryAddressDto dto = new DeliveryAddressDto();
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setZipCode(address.getZipCode());
        dto.setCountry(address.getCountry());
        dto.setInstructions(address.getInstructions());
        
        return dto;
    }
    
    public DeliveryAddress toDeliveryAddress(DeliveryAddressDto dto) {
        if (dto == null) {
            return null;
        }
        
        DeliveryAddress address = new DeliveryAddress();
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setZipCode(dto.getZipCode());
        address.setCountry(dto.getCountry());
        address.setInstructions(dto.getInstructions());
        
        return address;
    }
}