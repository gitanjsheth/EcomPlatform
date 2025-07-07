package com.gitanjsheth.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.services.CategoryServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Qualifier("selfCategoryService")
    private CategoryServiceInterface categoryServiceInterface;

    @Autowired
    private ObjectMapper objectMapper;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setTitle("Electronics");
    }

    @Test
    void getSingleCategory_ExistingCategory_ReturnsCategory() throws Exception {
        // Arrange
        when(categoryServiceInterface.getSingleCategory(1L)).thenReturn(testCategory);

        // Act & Assert
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Electronics"));

        verify(categoryServiceInterface, times(1)).getSingleCategory(1L);
    }

    @Test
    void getSingleCategory_NonExistingCategory_ReturnsNotFound() throws Exception {
        // Arrange
        when(categoryServiceInterface.getSingleCategory(1L)).thenThrow(new CategoryNotFoundException("Category not found with id: 1"));

        // Act & Assert
        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isNotFound());

        verify(categoryServiceInterface, times(1)).getSingleCategory(1L);
    }

    @Test
    void getAllCategories_ReturnsListOfCategories() throws Exception {
        // Arrange
        Category anotherCategory = new Category();
        anotherCategory.setId(2L);
        anotherCategory.setTitle("Clothing");
        
        List<Category> categories = Arrays.asList(testCategory, anotherCategory);
        when(categoryServiceInterface.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/categories/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Electronics"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Clothing"));

        verify(categoryServiceInterface, times(1)).getAllCategories();
    }

    @Test
    void getAllCategories_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(categoryServiceInterface.getAllCategories()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/categories/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryServiceInterface, times(1)).getAllCategories();
    }

    @Test
    void createCategory_ValidCategory_ReturnsCreatedCategory() throws Exception {
        // Arrange
        when(categoryServiceInterface.createCategory(any(Category.class))).thenReturn(testCategory);

        // Act & Assert
        mockMvc.perform(post("/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testCategory)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Electronics"));

        verify(categoryServiceInterface, times(1)).createCategory(any(Category.class));
    }

    @Test
    void createCategory_InvalidCategory_ReturnsBadRequest() throws Exception {
        // Arrange
        Category invalidCategory = new Category();
        // Missing required title field

        // Act & Assert
        mockMvc.perform(post("/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryServiceInterface, never()).createCategory(any(Category.class));
    }

    @Test
    void createCategory_TitleTooShort_ReturnsBadRequest() throws Exception {
        // Arrange
        Category invalidCategory = new Category();
        invalidCategory.setTitle("A"); // Too short (min 2 characters)

        // Act & Assert
        mockMvc.perform(post("/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryServiceInterface, never()).createCategory(any(Category.class));
    }

    @Test
    void createCategory_TitleTooLong_ReturnsBadRequest() throws Exception {
        // Arrange
        Category invalidCategory = new Category();
        invalidCategory.setTitle("A".repeat(51)); // Too long (max 50 characters)

        // Act & Assert
        mockMvc.perform(post("/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryServiceInterface, never()).createCategory(any(Category.class));
    }

    @Test
    void updateCategory_ValidCategory_ReturnsUpdatedCategory() throws Exception {
        // Arrange
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setTitle("Updated Electronics");
        
        when(categoryServiceInterface.updateCategory(anyLong(), any(Category.class))).thenReturn(updatedCategory);

        // Act & Assert
        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCategory)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Electronics"));

        verify(categoryServiceInterface, times(1)).updateCategory(eq(1L), any(Category.class));
    }

    @Test
    void updateCategory_InvalidCategory_ReturnsBadRequest() throws Exception {
        // Arrange
        Category invalidCategory = new Category();
        // Missing required title field

        // Act & Assert
        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryServiceInterface, never()).updateCategory(anyLong(), any(Category.class));
    }

    @Test
    void deleteCategory_ExistingCategory_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(categoryServiceInterface).deleteCategory(1L);

        // Act & Assert
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryServiceInterface, times(1)).deleteCategory(1L);
    }

    @Test
    void deleteCategory_ValidId_CallsServiceMethod() throws Exception {
        // Arrange
        doNothing().when(categoryServiceInterface).deleteCategory(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryServiceInterface, times(1)).deleteCategory(1L);
    }

    @Test
    void softDeleteCategory_ExistingCategory_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(categoryServiceInterface).softDeleteCategory(1L);

        // Act & Assert
        mockMvc.perform(patch("/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryServiceInterface, times(1)).softDeleteCategory(1L);
    }

    @Test
    void softDeleteCategory_ValidId_CallsServiceMethod() throws Exception {
        // Arrange
        doNothing().when(categoryServiceInterface).softDeleteCategory(anyLong());

        // Act & Assert
        mockMvc.perform(patch("/categories/1"))
                .andExpect(status().isNoContent());

        verify(categoryServiceInterface, times(1)).softDeleteCategory(1L);
    }

    @Test
    void createCategory_BlankTitle_ReturnsBadRequest() throws Exception {
        // Arrange
        Category invalidCategory = new Category();
        invalidCategory.setTitle("   "); // Blank title

        // Act & Assert
        mockMvc.perform(post("/categories/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryServiceInterface, never()).createCategory(any(Category.class));
    }

    @Test
    void updateCategory_BlankTitle_ReturnsBadRequest() throws Exception {
        // Arrange
        Category invalidCategory = new Category();
        invalidCategory.setTitle(""); // Empty title

        // Act & Assert
        mockMvc.perform(put("/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidCategory)))
                .andExpect(status().isBadRequest());

        verify(categoryServiceInterface, never()).updateCategory(anyLong(), any(Category.class));
    }
} 