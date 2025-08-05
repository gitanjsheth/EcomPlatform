package com.gitanjsheth.cartservice.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.cartservice.repositories.CartCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class ProductValidationService {
    
    private final RestTemplate restTemplate;
    private final CartCacheRepository cartCacheRepository;
    private final ObjectMapper objectMapper;
    
    @Value("${app.product-service.url}")
    private String productServiceUrl;
    
    public ProductValidationService(RestTemplate restTemplate, 
                                  CartCacheRepository cartCacheRepository,
                                  ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.cartCacheRepository = cartCacheRepository;
        this.objectMapper = objectMapper;
    }
    
    public boolean isProductAvailableForCart(Long productId, Integer requestedQuantity) {
        try {
            // Check cache first
            Optional<CartCacheRepository.ProductAvailability> cached = 
                cartCacheRepository.getCachedProductAvailability(productId);
            
            if (cached.isPresent()) {
                CartCacheRepository.ProductAvailability availability = cached.get();
                return availability.isAvailable() && availability.getAvailableQuantity() >= requestedQuantity;
            }
            
            // Fetch from product service
            String url = productServiceUrl + "/products/" + productId + "/availability";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                
                boolean isActive = responseBody.get("isActive").asBoolean();
                boolean isOutOfStock = responseBody.get("isOutOfStock").asBoolean();
                boolean allowBackorder = responseBody.get("allowBackorder").asBoolean();
                int availableQuantity = responseBody.get("availableQuantity").asInt();
                
                boolean isAvailable = isActive && (!isOutOfStock || allowBackorder);
                boolean hasEnoughQuantity = allowBackorder || availableQuantity >= requestedQuantity;
                
                // Cache the result
                cartCacheRepository.cacheProductAvailability(productId, isAvailable, availableQuantity);
                
                return isAvailable && hasEnoughQuantity;
            }
            
            return false;
        } catch (Exception e) {
            log.error("Error checking product availability for product {}: {}", productId, e.getMessage());
            return false;
        }
    }
    
    public ProductAvailabilityInfo checkProductAvailability(Long productId) {
        try {
            String url = productServiceUrl + "/products/" + productId + "/availability";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                
                boolean isActive = responseBody.get("isActive").asBoolean();
                boolean isOutOfStock = responseBody.get("isOutOfStock").asBoolean();
                boolean allowBackorder = responseBody.get("allowBackorder").asBoolean();
                int availableQuantity = responseBody.get("availableQuantity").asInt();
                
                boolean isAvailable = isActive && (!isOutOfStock || allowBackorder);
                
                return new ProductAvailabilityInfo(isAvailable, isOutOfStock, availableQuantity);
            }
            
            return new ProductAvailabilityInfo(false, true, 0);
        } catch (Exception e) {
            log.error("Error checking product availability for product {}: {}", productId, e.getMessage());
            return new ProductAvailabilityInfo(false, true, 0);
        }
    }
    
    public ProductDetails getProductDetails(Long productId) {
        try {
            String url = productServiceUrl + "/products/" + productId;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, null, String.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                JsonNode responseBody = objectMapper.readTree(response.getBody());
                
                String title = responseBody.get("title").asText();
                BigDecimal price = new BigDecimal(responseBody.get("price").asText());
                String imageUrl = responseBody.has("imageURL") ? responseBody.get("imageURL").asText() : null;
                
                return new ProductDetails(productId, title, price, imageUrl);
            }
            
            throw new RuntimeException("Product not found: " + productId);
        } catch (Exception e) {
            log.error("Error fetching product details for product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Failed to fetch product details", e);
        }
    }
    
    // Data classes
    
    public static class ProductAvailabilityInfo {
        private final boolean available;
        private final boolean outOfStock;
        private final int availableQuantity;
        
        public ProductAvailabilityInfo(boolean available, boolean outOfStock, int availableQuantity) {
            this.available = available;
            this.outOfStock = outOfStock;
            this.availableQuantity = availableQuantity;
        }
        
        public boolean isAvailable() { return available; }
        public boolean isOutOfStock() { return outOfStock; }
        public int getAvailableQuantity() { return availableQuantity; }
    }
    
    public static class ProductDetails {
        private final Long id;
        private final String title;
        private final BigDecimal price;
        private final String imageUrl;
        
        public ProductDetails(Long id, String title, BigDecimal price, String imageUrl) {
            this.id = id;
            this.title = title;
            this.price = price;
            this.imageUrl = imageUrl;
        }
        
        public Long getId() { return id; }
        public String getTitle() { return title; }
        public BigDecimal getPrice() { return price; }
        public String getImageUrl() { return imageUrl; }
    }
}