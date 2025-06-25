package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("selfProductService")
public class SelfProductService implements ProductServiceInterface{

    private ProductRepository productRepository;
    public SelfProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundException {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }

    @Override
    public Product createProduct(Product product) {
        return null;
    }

    @Override
    public Product updateProduct(Long productId, Product product) {
        return null;
    }

    @Override
    public Product changeProduct(Long productId, Product product) {
        return null;
    }

    @Override
    public Boolean deleteProduct(Long productId) {
        return null;
    }
}
