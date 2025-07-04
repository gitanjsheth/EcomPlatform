package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;

import java.util.List;

public interface ProductServiceInterface {
    Product getSingleProduct(Long productId) throws ProductNotFoundException;
    List<Product> getAllProducts();
    Product createProduct(Product product) throws CategoryNotFoundException;
    Product updateProduct(Long productId, Product product);
    Product changeProduct(Long productId, Product product);
    void deleteProduct(Long productId);
    void softDeleteById(Long productId);
}
