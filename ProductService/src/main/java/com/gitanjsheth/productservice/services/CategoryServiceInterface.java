package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.models.Category;

import java.util.List;

public interface CategoryServiceInterface {
    Category getSingleCategory(Long categoryId) throws CategoryNotFoundException;
    List<Category> getAllCategories();
    Category createCategory(Category category);
    Category updateCategory(Long categoryId, Category category) throws CategoryNotFoundException;
    void deleteCategory(Long categoryId) throws CategoryNotFoundException;
    void softDeleteCategory(Long categoryId) throws CategoryNotFoundException;
} 