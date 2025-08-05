package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.dtos.ProductAvailabilityDto;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.repositories.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

@Service
@Slf4j
public class InventoryService {
    
    private static final int HOLD_DURATION_HOURS = 24;
    private static final int CLEANUP_INTERVAL_HOURS = 1;
    
    private final ProductRepository productRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final ConcurrentHashMap<String, InventoryHold> inventoryHolds = new ConcurrentHashMap<>();
    
    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        startInventoryHoldCleanup();
    }
    
    // ============================================================================
    // PUBLIC API METHODS
    // ============================================================================
    
    /**
     * Check if product is available for cart addition
     */
    public boolean isProductAvailableForCart(Long productId, Integer requestedQuantity) {
        return executeWithProduct(productId, 
            product -> product.isAvailableForCart(requestedQuantity),
            false,
            "checking product availability for cart");
    }
    
    /**
     * Temporarily reserve inventory for checkout (24-hour hold)
     */
    @Transactional
    public boolean reserveInventoryForCheckout(Long productId, Integer quantity, Long userId) {
        return executeWithProduct(productId, product -> {
            // Check if enough inventory available
            if (product.getAvailableQuantity() < quantity) {
                log.warn("Insufficient inventory for product {}: requested={}, available={}", 
                    productId, quantity, product.getAvailableQuantity());
                return false;
            }
            
            // Reserve inventory in database
            product.setReservedQuantity(product.getReservedQuantity() + quantity);
            saveProductWithStockUpdate(product);
            
            // Create temporary hold
            createInventoryHold(productId, userId, quantity);
            
            log.info("Reserved {} units of product {} for user {}", quantity, productId, userId);
            return true;
        }, false, "reserving inventory");
    }
    
    /**
     * Release reserved inventory (on successful order or cancellation)
     */
    @Transactional
    public void releaseReservedInventory(Long productId, Integer quantity, Long userId) {
        executeWithProductVoid(productId, product -> {
            // Release from database
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - quantity));
            saveProductWithStockUpdate(product);
            
            // Remove temporary hold
            removeInventoryHold(productId, userId);
            
            log.info("Released {} units of product {} for user {}", quantity, productId, userId);
        }, "releasing inventory");
    }
    
    /**
     * Confirm inventory usage (on successful order completion)
     */
    @Transactional
    public void confirmInventoryUsage(Long productId, Integer quantity, Long userId) {
        executeWithProductVoid(productId, product -> {
            // Reduce actual stock and reserved quantity
            product.setStockQuantity(Math.max(0, product.getStockQuantity() - quantity));
            product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - quantity));
            saveProductWithStockUpdate(product);
            
            // Remove temporary hold
            removeInventoryHold(productId, userId);
            
            log.info("Confirmed usage of {} units of product {} for user {}", quantity, productId, userId);
        }, "confirming inventory usage");
    }
    
    /**
     * Update stock quantity (for admin operations)
     */
    @Transactional
    public void updateStockQuantity(Long productId, Integer newStockQuantity) {
        executeWithProductVoid(productId, product -> {
            product.setStockQuantity(newStockQuantity);
            saveProductWithStockUpdate(product);
            
            log.info("Updated stock quantity for product {} to {}", productId, newStockQuantity);
        }, "updating stock quantity");
    }
    
    /**
     * Get product availability information
     */
    public ProductAvailabilityDto getProductAvailability(Long productId) throws ProductNotFoundException {
        Product product = findProductById(productId);
        return new ProductAvailabilityDto(
            product.getId(),
            product.getTitle(),
            product.getIsActive(),
            product.getIsOutOfStock(),
            product.getAllowBackorder(),
            product.getStockQuantity(),
            product.getReservedQuantity(),
            product.getAvailableQuantity(),
            product.getLowStockThreshold(),
            product.getShowWhenOutOfStock()
        );
    }
    
    /**
     * Get products that are out of stock or low in stock
     */
    public List<Product> getOutOfStockProducts() {
        return productRepository.findByIsOutOfStockTrue();
    }
    
    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }
    
    /**
     * Validate cart items against current inventory
     */
    public boolean validateCartItems(List<CartItemValidation> cartItems) {
        return cartItems.stream()
            .allMatch(item -> isProductAvailableForCart(item.getProductId(), item.getQuantity()));
    }
    
    // ============================================================================
    // PRIVATE HELPER METHODS
    // ============================================================================
    
    /**
     * Execute operation with product, handling exceptions and returning a result
     */
    private <T> T executeWithProduct(Long productId, Function<Product, T> operation, 
                                    T defaultValue, String operationName) {
        try {
            Product product = findProductById(productId);
            return operation.apply(product);
        } catch (Exception e) {
            log.error("Error {}: {}", operationName, e.getMessage());
            return defaultValue;
        }
    }
    
    /**
     * Execute operation with product, handling exceptions (void return)
     */
    private void executeWithProductVoid(Long productId, Consumer<Product> operation, String operationName) {
        try {
            Product product = findProductById(productId);
            operation.accept(product);
        } catch (Exception e) {
            log.error("Error {}: {}", operationName, e.getMessage());
        }
    }
    
    /**
     * Find product by ID with consistent error handling
     */
    private Product findProductById(Long productId) throws ProductNotFoundException {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId, "Product not found"));
    }
    
    /**
     * Save product with stock status update
     */
    private void saveProductWithStockUpdate(Product product) {
        product.updateOutOfStockStatus();
        productRepository.save(product);
    }
    
    /**
     * Create inventory hold
     */
    private void createInventoryHold(Long productId, Long userId, Integer quantity) {
        String holdKey = generateHoldKey(productId, userId);
        InventoryHold hold = new InventoryHold(productId, userId, quantity, 
            LocalDateTime.now().plusHours(HOLD_DURATION_HOURS));
        inventoryHolds.put(holdKey, hold);
    }
    
    /**
     * Remove inventory hold
     */
    private void removeInventoryHold(Long productId, Long userId) {
        String holdKey = generateHoldKey(productId, userId);
        inventoryHolds.remove(holdKey);
    }
    
    /**
     * Generate unique hold key
     */
    private String generateHoldKey(Long productId, Long userId) {
        return productId + ":" + userId;
    }
    
    /**
     * Start background cleanup task for expired holds
     */
    private void startInventoryHoldCleanup() {
        scheduler.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now();
            inventoryHolds.entrySet().removeIf(entry -> {
                InventoryHold hold = entry.getValue();
                if (hold.getExpiryTime().isBefore(now)) {
                    releaseReservedInventory(hold.getProductId(), hold.getQuantity(), hold.getUserId());
                    log.info("Released expired inventory hold: {}", entry.getKey());
                    return true;
                }
                return false;
            });
        }, CLEANUP_INTERVAL_HOURS, CLEANUP_INTERVAL_HOURS, TimeUnit.HOURS);
    }
    
    // ============================================================================
    // INNER CLASSES
    // ============================================================================
    
    /**
     * DTO for cart item validation
     */
    public static class CartItemValidation {
        private final Long productId;
        private final Integer quantity;
        
        public CartItemValidation(Long productId, Integer quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        
        public Long getProductId() { return productId; }
        public Integer getQuantity() { return quantity; }
    }
    
    /**
     * Internal class for tracking inventory holds
     */
    private static class InventoryHold {
        private final Long productId;
        private final Long userId;
        private final Integer quantity;
        private final LocalDateTime expiryTime;
        
        public InventoryHold(Long productId, Long userId, Integer quantity, LocalDateTime expiryTime) {
            this.productId = productId;
            this.userId = userId;
            this.quantity = quantity;
            this.expiryTime = expiryTime;
        }
        
        public Long getProductId() { return productId; }
        public Long getUserId() { return userId; }
        public Integer getQuantity() { return quantity; }
        public LocalDateTime getExpiryTime() { return expiryTime; }
    }
}