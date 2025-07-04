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
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId));
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
    public Product updateProduct(Long productId, Product product) {
        return null;
    }

    @Override
    public Product changeProduct(Long productId, Product product) {
        return null;
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
