package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.repositories.ProductRepository;
import com.gitanjsheth.productservice.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service("selfProductService")
public class SelfProductService implements ProductServiceInterface{

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;

    public SelfProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundException {
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId, "Product not found with id: " + productId));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product createProduct(Product product) throws CategoryNotFoundException {
        Category category = product.getCategory();
        if (category == null) {
            throw new CategoryNotFoundException("Need a category to add product.");
        }
        Optional<Category> optionalCategory = categoryRepository.findByTitle(category.getTitle());
        if (optionalCategory.isEmpty()) {
            category = categoryRepository.save(category);
        }
        else {
            category = optionalCategory.get();
        }
        product.setCategory(category);

        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductNotFoundException, CategoryNotFoundException {
        // PUT operation - but handle partial input gracefully
        // Load existing product first
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId, "Product not found with id: " + productId));

        // Handle category if provided
        if (product.getCategory() != null) {
            Category category = product.getCategory();
            Optional<Category> optionalCategory = categoryRepository.findByTitle(category.getTitle());
            if (optionalCategory.isEmpty()) {
                category = categoryRepository.save(category);
            } else {
                category = optionalCategory.get();
            }
            existingProduct.setCategory(category);
        }

        // Update only provided fields (graceful handling)
        if (product.getTitle() != null && !product.getTitle().trim().isEmpty()) {
            existingProduct.setTitle(product.getTitle());
        }
        if (product.getDescription() != null) {
            existingProduct.setDescription(product.getDescription());
        }
        if (product.getPrice() != null) {
            existingProduct.setPrice(product.getPrice());
        }
        if (product.getImageURL() != null) {
            existingProduct.setImageURL(product.getImageURL());
        }

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(Long productId) {
        productRepository.deleteById(productId);
    }

    @Override
    public void softDeleteById(Long productId) {
        productRepository.softDeleteById(productId);
    }
}
