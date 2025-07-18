package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SelfCategoryService implements CategoryServiceInterface {

    private final CategoryRepository categoryRepository;

    public SelfCategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category getSingleCategory(Long categoryId) throws CategoryNotFoundException {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId, categoryId));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long categoryId, Category category) throws CategoryNotFoundException {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        // PUT operation - but handle partial input gracefully
        // Load existing category first
        Category existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with id: " + categoryId, categoryId));
        
        // Update only provided fields (graceful handling)
        if (category.getTitle() != null && !category.getTitle().trim().isEmpty()) {
            existingCategory.setTitle(category.getTitle());
        }
        
        return categoryRepository.save(existingCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) throws CategoryNotFoundException {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
        
        // Verify category exists before deletion
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Cannot delete - Category not found with id: " + categoryId, categoryId);
        }
        
        // Use hard delete for permanent removal
        categoryRepository.hardDeleteById(categoryId);
    }
    
    @Override
    @Transactional
    public void softDeleteCategory(Long categoryId) throws CategoryNotFoundException {
        if (categoryId == null || categoryId <= 0) {
            throw new IllegalArgumentException("Category ID must be a positive number");
        }
        
        // Verify category exists before soft deletion
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFoundException("Cannot soft delete - Category not found with id: " + categoryId, categoryId);
        }
        
        // deleteById now triggers soft delete via @SQLDelete annotation
        categoryRepository.deleteById(categoryId);
    }


} 