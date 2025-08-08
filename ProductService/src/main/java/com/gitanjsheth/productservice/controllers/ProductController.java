package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.security.SecurityUtils;
import com.gitanjsheth.productservice.security.UserPrincipal;
import com.gitanjsheth.productservice.services.ProductServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"}) // Restrict to specific origins
public class ProductController {

    private final ProductServiceInterface productService;

    public ProductController(@Qualifier("selfProductService") ProductServiceInterface productService) {
        this.productService = productService;
    }

    //localhost:8081/products/10
    @GetMapping("/{id}")
    public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId) throws ProductNotFoundException {
        Product product = productService.getSingleProduct(productId);
        return new ResponseEntity<>(product, HttpStatus.OK);
    }

    //localhost:8081/products/ (with trailing slash)
    @GetMapping("/")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    //localhost:8081/products (without trailing slash)
    @GetMapping
    public ResponseEntity<List<Product>> getAllProductsNoSlash() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    //localhost:8081/products/
    @PostMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) throws CategoryNotFoundException {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            org.slf4j.LoggerFactory.getLogger(ProductController.class)
                    .info("Product being created by: {}", currentUser.getUsername());
        }
        
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    //localhost:8081/products/10
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long productId, @Valid @RequestBody Product product) 
            throws ProductNotFoundException, CategoryNotFoundException {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            org.slf4j.LoggerFactory.getLogger(ProductController.class)
                    .info("Product {} being updated by: {}", productId, currentUser.getUsername());
        }
        
        Product updatedProduct = productService.updateProduct(productId, product);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    //localhost:8081/products/10 (Hard delete - completely removes from database)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long productId) throws ProductNotFoundException {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            org.slf4j.LoggerFactory.getLogger(ProductController.class)
                    .info("Product {} being HARD DELETED by: {}", productId, currentUser.getUsername());
        }
        
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //localhost:8081/products/10 (Soft delete - marks as deleted but keeps in database)
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable("id") Long productId) throws ProductNotFoundException {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            org.slf4j.LoggerFactory.getLogger(ProductController.class)
                    .info("Product {} being SOFT DELETED by: {}", productId, currentUser.getUsername());
        }
        
        productService.softDeleteById(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    // Test endpoint to check authentication
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser != null) {
            return ResponseEntity.ok(currentUser);
        } else {
            return ResponseEntity.ok("Not authenticated");
        }
    }

    // User-specific endpoints - accessible by any authenticated user (USER or ADMIN)
    @GetMapping("/wishlist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserWishlist() {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        
        // In a real application, this would fetch from a wishlist service/repository
        // For demo purposes, we'll return a mock response
        return ResponseEntity.ok(java.util.Map.of(
            "userId", currentUser.getUserId(),
            "username", currentUser.getUsername(),
            "wishlistItems", java.util.List.of(
                java.util.Map.of("productId", 1, "productName", "Sample Product 1", "addedDate", "2024-01-15"),
                java.util.Map.of("productId", 3, "productName", "Sample Product 3", "addedDate", "2024-01-20")
            ),
            "message", "Wishlist retrieved successfully for user: " + currentUser.getUsername()
        ));
    }
    
    @PostMapping("/wishlist/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addToWishlist(@PathVariable("productId") Long productId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        
        // In a real application, this would add to wishlist service/repository
        // For demo purposes, we'll return a mock response
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Product " + productId + " added to wishlist for user: " + currentUser.getUsername(),
            "userId", currentUser.getUserId(),
            "productId", productId,
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    @DeleteMapping("/wishlist/{productId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> removeFromWishlist(@PathVariable("productId") Long productId) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        
        // In a real application, this would remove from wishlist service/repository
        // For demo purposes, we'll return a mock response
        return ResponseEntity.ok(java.util.Map.of(
            "message", "Product " + productId + " removed from wishlist for user: " + currentUser.getUsername(),
            "userId", currentUser.getUserId(),
            "productId", productId,
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    // User profile endpoint - accessible by any authenticated user
    @GetMapping("/user/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserProfile() {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }
        
        return ResponseEntity.ok(java.util.Map.of(
            "userId", currentUser.getUserId(),
            "username", currentUser.getUsername(),
            "email", currentUser.getEmail(),
            "roles", currentUser.getRoles(),
            "accessLevel", currentUser.hasRole("ADMIN") ? "Administrator" : "Regular User",
            "message", "Profile retrieved successfully"
        ));
    }

}
