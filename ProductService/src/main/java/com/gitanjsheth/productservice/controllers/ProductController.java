package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.services.ProductServiceInterface;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final RestTemplate restTemplate;
    private ProductServiceInterface productService;

    public ProductController(@Qualifier("selfProductService") ProductServiceInterface productService) {
        this.productService = productService;
        this.restTemplate = new RestTemplate();
    }

    //localhost:8080/products/10
    @GetMapping("/{id}")
    public ResponseEntity<Product> getSingleProduct(@PathVariable("id") Long productId) throws ProductNotFoundException {
        ResponseEntity<Product> response =
                new ResponseEntity<>(
                        productService.getSingleProduct(productId),
                        HttpStatus.OK);
        return response;
    }

    //localhost:8080/products/
    @GetMapping("/")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    //localhost:8080/products/10
    @DeleteMapping("/{id}")
    public Boolean deleteProduct(@PathVariable("id") Long productId) {
        System.out.println("Deleted product: " + productId);
        return true;
    }

    //localhost:8080/products/
    @PostMapping("/")
    public Product createProduct(@RequestBody Product product) throws CategoryNotFoundException {
        return productService.createProduct(product);
    }

    //localhost:8080/products/
    @PatchMapping("/")
    public Product updateProduct(@RequestBody Product product) {
        return new Product();
    }

    @PutMapping("/")
    public Product replaceProduct(@RequestBody Product product) {
        return new Product();
    }

}
