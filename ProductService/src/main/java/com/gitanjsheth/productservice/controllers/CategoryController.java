package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.services.CategoryServiceInterface;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryServiceInterface categoryServiceInterface;

    public CategoryController(CategoryServiceInterface categoryServiceInterface) {
        this.categoryServiceInterface = categoryServiceInterface;
    }

    //localhost:8081/categories/10
    @GetMapping("/{id}")
    public ResponseEntity<Category> getSingleCategory(@PathVariable("id") Long categoryId) throws CategoryNotFoundException {
        Category category = categoryServiceInterface.getSingleCategory(categoryId);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    //localhost:8081/categories/
    @GetMapping("/")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryServiceInterface.getAllCategories();
        return new ResponseEntity<>(categories, HttpStatus.OK);
    }

    //localhost:8081/categories/
    @PostMapping("/")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category createdCategory = categoryServiceInterface.createCategory(category);
        return new ResponseEntity<>(createdCategory, HttpStatus.CREATED);
    }

    //localhost:8081/categories/10
    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(@PathVariable("id") Long categoryId, @Valid @RequestBody Category category) 
            throws CategoryNotFoundException {
        Category updatedCategory = categoryServiceInterface.updateCategory(categoryId, category);
        return new ResponseEntity<>(updatedCategory, HttpStatus.OK);
    }

    //localhost:8081/categories/10
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("id") Long categoryId) {
        categoryServiceInterface.deleteCategory(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    //localhost:8081/categories/10 (Soft delete - marks as deleted but keeps in database)
    @PatchMapping("/{id}")
    public ResponseEntity<Void> softDeleteCategory(@PathVariable("id") Long categoryId) {
        categoryServiceInterface.softDeleteCategory(categoryId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
} 