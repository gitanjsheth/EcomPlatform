package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.services.ProductServiceInterface;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
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

    //localhost:8081/products/
    @GetMapping("/")
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        return new ResponseEntity<>(products, HttpStatus.OK);
    }

    //localhost:8081/products/
    @PostMapping("/")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) throws CategoryNotFoundException {
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    //localhost:8081/products/10
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long productId, @Valid @RequestBody Product product) 
            throws ProductNotFoundException, CategoryNotFoundException {
        Product updatedProduct = productService.updateProduct(productId, product);
        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    //localhost:8081/products/10 (Hard delete - completely removes from database)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable("id") Long productId) {
        productService.deleteProduct(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //localhost:8081/products/10 (Soft delete - marks as deleted but keeps in database)
    @PatchMapping("/{id}")
    public ResponseEntity<Void> softDeleteProduct(@PathVariable("id") Long productId) {
        productService.softDeleteById(productId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
