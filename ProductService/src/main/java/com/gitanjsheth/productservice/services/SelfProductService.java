package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.repositories.ProductRepository;
import com.gitanjsheth.productservice.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service("selfProductService")
public class SelfProductService implements ProductServiceInterface{

    private ProductRepository productRepository;
    private CategoryRepository categoryRepository;
    private final SearchService searchService;

    public SelfProductService(ProductRepository productRepository, CategoryRepository categoryRepository, SearchService searchService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.searchService = searchService;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundException {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        return productRepository.findById(productId).orElseThrow(() -> new ProductNotFoundException(productId, "Product not found with id: " + productId));
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product createProduct(Product product) throws CategoryNotFoundException {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        Category category = product.getCategory();
        if (category == null) {
            throw new CategoryNotFoundException("Need a category to add product.");
        }
        
        Category resolvedCategory = getOrCreateCategory(category);
        product.setCategory(resolvedCategory);

        Product saved = productRepository.save(product);
        searchService.indexProduct(saved);
        return saved;
    }

    @Override
    @Transactional
    public Product updateProduct(Long productId, Product product) throws ProductNotFoundException, CategoryNotFoundException {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        // PUT operation - but handle partial input gracefully
        // Load existing product first
        Product existingProduct = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId, "Product not found with id: " + productId));

        // Handle category if provided
        if (product.getCategory() != null) {
            Category resolvedCategory = getOrCreateCategory(product.getCategory());
            existingProduct.setCategory(resolvedCategory);
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

        Product saved = productRepository.save(existingProduct);
        searchService.indexProduct(saved);
        return saved;
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) throws ProductNotFoundException {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        
        // Verify product exists before deletion
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId, "Cannot delete - Product not found with id: " + productId);
        }
        
        // Use hard delete for permanent removal
        productRepository.hardDeleteById(productId);
        searchService.deleteProductIndex(productId);
    }

    @Override
    @Transactional
    public void softDeleteById(Long productId) throws ProductNotFoundException {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("Product ID must be a positive number");
        }
        
        // Verify product exists before soft deletion
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId, "Cannot soft delete - Product not found with id: " + productId);
        }
        
        // deleteById now triggers soft delete via @SQLDelete annotation
        productRepository.deleteById(productId);
        searchService.deleteProductIndex(productId);
    }
    
    /**
     * Gets an existing category by title or creates a new one if it doesn't exist
     * Uses proper entity state management to handle concurrent scenarios
     */
    private Category getOrCreateCategory(Category category) {
        if (category.getTitle() == null || category.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Category title cannot be null or empty");
        }
        
        // Try to find existing category first
        Optional<Category> existingCategory = categoryRepository.findByTitle(category.getTitle());
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }
        
        // Create new category if not found
        // Set only the title from input, let JPA handle other fields
        Category newCategory = new Category();
        newCategory.setTitle(category.getTitle());
        
        try {
            return categoryRepository.save(newCategory);
        } catch (Exception e) {
            // Handle potential race condition - another thread might have created the category
            Optional<Category> raceCheckCategory = categoryRepository.findByTitle(category.getTitle());
            if (raceCheckCategory.isPresent()) {
                return raceCheckCategory.get();
            }
            throw new RuntimeException("Failed to create or find category: " + category.getTitle(), e);
        }
    }
}
