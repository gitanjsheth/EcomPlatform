package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;

import java.util.List;

public interface ProductServiceInterface {
    Product getSingleProduct(Long productId) throws ProductNotFoundException;
    List<Product> getAllProducts();
    Product createProduct(Product product);
    Product updateProduct(Long productId, Product product);
    Product changeProduct(Long productId, Product product);
    Boolean deleteProduct(Long productId);
}
