package com.gitanjsheth.productservice.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.services.ProductServiceInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = ProductController.class, useDefaultFilters = false)
@ComponentScan(basePackages = "com.gitanjsheth.productservice.controllers")
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    @Qualifier("selfProductService")
    private ProductServiceInterface productService;

    @Autowired
    private ObjectMapper objectMapper;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setTitle("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("Test Product");
        testProduct.setPrice(100);
        testProduct.setDescription("Test Description");
        testProduct.setImageURL("http://test.com/image.jpg");
        testProduct.setCategory(testCategory);
    }

    @Test
    void getSingleProduct_ExistingProduct_ReturnsProduct() throws Exception {
        // Arrange
        when(productService.getSingleProduct(1L)).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Product"))
                .andExpect(jsonPath("$.price").value(100))
                .andExpect(jsonPath("$.description").value("Test Description"));

        verify(productService, times(1)).getSingleProduct(1L);
    }

    @Test
    void getSingleProduct_NonExistingProduct_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.getSingleProduct(1L)).thenThrow(new ProductNotFoundException(1L));

        // Act & Assert
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).getSingleProduct(1L);
    }

    @Test
    void getAllProducts_ReturnsListOfProducts() throws Exception {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productService.getAllProducts()).thenReturn(products);

        // Act & Assert
        mockMvc.perform(get("/products/"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Product"));

        verify(productService, times(1)).getAllProducts();
    }

    @Test
    void createProduct_ValidProduct_ReturnsCreatedProduct() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(post("/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Product"));

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void createProduct_InvalidProduct_ReturnsBadRequest() throws Exception {
        // Arrange
        Product invalidProduct = new Product();
        // Missing required fields (title, category)

        // Act & Assert
        mockMvc.perform(post("/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).createProduct(any(Product.class));
    }

    @Test
    void createProduct_CategoryNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.createProduct(any(Product.class)))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        // Act & Assert
        mockMvc.perform(post("/products/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).createProduct(any(Product.class));
    }

    @Test
    void updateProduct_ValidProduct_ReturnsUpdatedProduct() throws Exception {
        // Arrange
        when(productService.updateProduct(anyLong(), any(Product.class))).thenReturn(testProduct);

        // Act & Assert
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1));

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_InvalidProduct_ReturnsBadRequest() throws Exception {
        // Arrange
        Product invalidProduct = new Product();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidProduct)))
                .andExpect(status().isBadRequest());

        verify(productService, never()).updateProduct(anyLong(), any(Product.class));
    }

    @Test
    void updateProduct_ProductNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.updateProduct(anyLong(), any(Product.class)))
                .thenThrow(new ProductNotFoundException(1L, "Product not found"));

        // Act & Assert
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void updateProduct_CategoryNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        when(productService.updateProduct(anyLong(), any(Product.class)))
                .thenThrow(new CategoryNotFoundException("Category not found"));

        // Act & Assert
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testProduct)))
                .andExpect(status().isNotFound());

        verify(productService, times(1)).updateProduct(eq(1L), any(Product.class));
    }

    @Test
    void deleteProduct_ExistingProduct_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act & Assert
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void deleteProduct_ValidId_CallsServiceMethod() throws Exception {
        // Arrange
        doNothing().when(productService).deleteProduct(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).deleteProduct(1L);
    }

    @Test
    void softDeleteProduct_ExistingProduct_ReturnsNoContent() throws Exception {
        // Arrange
        doNothing().when(productService).softDeleteById(1L);

        // Act & Assert
        mockMvc.perform(patch("/products/1/soft-delete"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).softDeleteById(1L);
    }

    @Test
    void softDeleteProduct_ValidId_CallsServiceMethod() throws Exception {
        // Arrange
        doNothing().when(productService).softDeleteById(anyLong());

        // Act & Assert
        mockMvc.perform(patch("/products/1/soft-delete"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).softDeleteById(1L);
    }
}