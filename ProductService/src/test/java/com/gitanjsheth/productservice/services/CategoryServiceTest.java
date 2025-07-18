package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SelfCategoryService categoryService;

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
        when(categoryRepository.findAll()).thenReturn(expectedCategories);

        // Act
        List<Category> actualCategories = categoryService.getAllCategories();

        // Assert
        assertEquals(expectedCategories, actualCategories);
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getSingleCategory_ValidId_ReturnsCategory() throws CategoryNotFoundException {
        // Arrange
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // Act
        Category actualCategory = categoryService.getSingleCategory(1L);

        // Assert
        assertEquals(testCategory, actualCategory);
        verify(categoryRepository, times(1)).findById(1L);
    }

    @Test
    void getSingleCategory_InvalidId_ThrowsException() {
        // Arrange
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception = assertThrows(
            CategoryNotFoundException.class,
            () -> categoryService.getSingleCategory(999L)
        );
        
        assertEquals("Category not found with id: 999", exception.getMessage());
        assertEquals(999L, exception.getCategoryId());
        verify(categoryRepository, times(1)).findById(999L);
    }

    @Test
    void createCategory_ValidCategory_ReturnsCreatedCategory() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setTitle("Books");
        
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        // Act
        Category actualCategory = categoryService.createCategory(newCategory);

        // Assert
        assertEquals(testCategory, actualCategory);
        verify(categoryRepository, times(1)).save(newCategory);
    }

    @Test
    void updateCategory_ValidIdAndCategory_ReturnsUpdatedCategory() throws CategoryNotFoundException {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setTitle("Original Title");
        
        Category updateRequest = new Category();
        updateRequest.setTitle("Updated Title");
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        // Act
        Category actualCategory = categoryService.updateCategory(1L, updateRequest);

        // Assert
        assertEquals("Updated Title", existingCategory.getTitle());
        assertEquals(existingCategory, actualCategory);
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void updateCategory_InvalidId_ThrowsException() {
        // Arrange
        Category updateRequest = new Category();
        updateRequest.setTitle("Updated Title");
        
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        CategoryNotFoundException exception = assertThrows(
            CategoryNotFoundException.class,
            () -> categoryService.updateCategory(999L, updateRequest)
        );
        
        assertEquals("Category not found with id: 999", exception.getMessage());
        assertEquals(999L, exception.getCategoryId());
        verify(categoryRepository, times(1)).findById(999L);
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_PartialData_PreservesExistingValues() throws CategoryNotFoundException {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setTitle("Original Title");
        
        Category partialUpdate = new Category();
        partialUpdate.setTitle(null); // Null title should preserve existing
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        // Act
        Category actualCategory = categoryService.updateCategory(1L, partialUpdate);

        // Assert
        assertEquals("Original Title", existingCategory.getTitle()); // Should remain unchanged
        assertEquals(existingCategory, actualCategory);
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void updateCategory_EmptyTitle_PreservesExistingValue() throws CategoryNotFoundException {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setId(1L);
        existingCategory.setTitle("Original Title");
        
        Category partialUpdate = new Category();
        partialUpdate.setTitle("   "); // Whitespace-only title should preserve existing
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        // Act
        Category actualCategory = categoryService.updateCategory(1L, partialUpdate);

        // Assert
        assertEquals("Original Title", existingCategory.getTitle()); // Should remain unchanged
        assertEquals(existingCategory, actualCategory);
        verify(categoryRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).save(existingCategory);
    }

    @Test
    void softDeleteCategory_ValidId_CallsRepositoryDeleteForSoftDelete() throws CategoryNotFoundException {
        // Arrange
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        // Act
        categoryService.softDeleteCategory(1L);

        // Assert
        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).deleteById(1L); // Now uses regular deleteById for soft delete
    }

    @Test
    void deleteCategory_ValidId_CallsHardDeleteRepository() throws CategoryNotFoundException {
        // Arrange
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).hardDeleteById(1L);

        // Act
        categoryService.deleteCategory(1L);

        // Assert
        verify(categoryRepository, times(1)).existsById(1L);
        verify(categoryRepository, times(1)).hardDeleteById(1L); // Now uses hardDeleteById for permanent removal
    }
} 