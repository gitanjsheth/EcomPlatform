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

    //localhost:8081/products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    //localhost:8081/products/category/{categoryId}
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable("categoryId") Long categoryId) {
        try {
            List<Product> products = productService.getProductsByCategory(categoryId);
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (CategoryNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    //localhost:8081/products/category/title/{categoryTitle}
    @GetMapping("/category/title/{categoryTitle}")
    public ResponseEntity<List<Product>> getProductsByCategoryTitle(@PathVariable("categoryTitle") String categoryTitle) {
        List<Product> products = productService.getProductsByCategoryTitle(categoryTitle);
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
}
