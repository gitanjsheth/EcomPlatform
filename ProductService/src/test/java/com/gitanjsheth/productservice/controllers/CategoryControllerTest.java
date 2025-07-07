package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.services.CategoryServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryServiceInterface categoryServiceInterface;

    @InjectMocks
    private CategoryController categoryController;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setTitle("Electronics");
    }

    @Test
    void getAllCategories_ReturnsListOfCategories() {
        // Arrange
        List<Category> expectedCategories = Arrays.asList(testCategory);
        when(categoryServiceInterface.getAllCategories()).thenReturn(expectedCategories);

        // Act
        ResponseEntity<List<Category>> response = categoryController.getAllCategories();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedCategories, response.getBody());
        verify(categoryServiceInterface, times(1)).getAllCategories();
    }

    @Test
    void getSingleCategory_ValidId_ReturnsCategory() throws CategoryNotFoundException {
        // Arrange
        when(categoryServiceInterface.getSingleCategory(1L)).thenReturn(testCategory);

        // Act
        ResponseEntity<Category> response = categoryController.getSingleCategory(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCategory, response.getBody());
        verify(categoryServiceInterface, times(1)).getSingleCategory(1L);
    }

    @Test
    void createCategory_ValidCategory_ReturnsCreatedCategory() {
        // Arrange
        when(categoryServiceInterface.createCategory(any(Category.class))).thenReturn(testCategory);

        // Act
        ResponseEntity<Category> response = categoryController.createCategory(testCategory);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testCategory, response.getBody());
        verify(categoryServiceInterface, times(1)).createCategory(testCategory);
    }

    @Test
    void updateCategory_ValidIdAndCategory_ReturnsUpdatedCategory() throws CategoryNotFoundException {
        // Arrange
        Category updatedCategory = new Category();
        updatedCategory.setTitle("Updated Electronics");
        when(categoryServiceInterface.updateCategory(eq(1L), any(Category.class))).thenReturn(updatedCategory);

        // Act
        ResponseEntity<Category> response = categoryController.updateCategory(1L, testCategory);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedCategory, response.getBody());
        verify(categoryServiceInterface, times(1)).updateCategory(1L, testCategory);
    }

    @Test
    void softDeleteCategory_ValidId_ReturnsNoContent() throws CategoryNotFoundException {
        // Arrange
        doNothing().when(categoryServiceInterface).softDeleteCategory(1L);

        // Act
        ResponseEntity<Void> response = categoryController.softDeleteCategory(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(categoryServiceInterface, times(1)).softDeleteCategory(1L);
    }

    @Test
    void deleteCategory_ValidId_ReturnsNoContent() throws CategoryNotFoundException {
        // Arrange
        doNothing().when(categoryServiceInterface).deleteCategory(1L);

        // Act
        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(categoryServiceInterface, times(1)).deleteCategory(1L);
    }
} 