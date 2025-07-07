package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.dtos.ExceptionDto;
import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.services.ProductServiceInterface;
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
class ProductControllerTest {

    @Mock
    private ProductServiceInterface productServiceInterface;

    @InjectMocks
    private ProductController productController;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setTitle("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("iPhone 15");
        testProduct.setDescription("Latest iPhone model");
        testProduct.setPrice(999);
        testProduct.setImageURL("http://example.com/image.jpg");
        testProduct.setCategory(testCategory);
    }

    @Test
    void getAllProducts_ReturnsListOfProducts() {
        // Arrange
        List<Product> expectedProducts = Arrays.asList(testProduct);
        when(productServiceInterface.getAllProducts()).thenReturn(expectedProducts);

        // Act
        ResponseEntity<List<Product>> response = productController.getAllProducts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedProducts, response.getBody());
        verify(productServiceInterface, times(1)).getAllProducts();
    }

    @Test
    void getSingleProduct_ValidId_ReturnsProduct() throws ProductNotFoundException {
        // Arrange
        when(productServiceInterface.getSingleProduct(1L)).thenReturn(testProduct);

        // Act
        ResponseEntity<Product> response = productController.getSingleProduct(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProduct, response.getBody());
        verify(productServiceInterface, times(1)).getSingleProduct(1L);
    }

    @Test
    void createProduct_ValidProduct_ReturnsCreatedProduct() throws CategoryNotFoundException {
        // Arrange
        when(productServiceInterface.createProduct(any(Product.class))).thenReturn(testProduct);

        // Act
        ResponseEntity<Product> response = productController.createProduct(testProduct);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testProduct, response.getBody());
        verify(productServiceInterface, times(1)).createProduct(testProduct);
    }

    @Test
    void updateProduct_ValidIdAndProduct_ReturnsUpdatedProduct() throws ProductNotFoundException, CategoryNotFoundException {
        // Arrange
        Product updatedProduct = new Product();
        updatedProduct.setTitle("Updated iPhone");
        when(productServiceInterface.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        // Act
        ResponseEntity<Product> response = productController.updateProduct(1L, testProduct);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedProduct, response.getBody());
        verify(productServiceInterface, times(1)).updateProduct(1L, testProduct);
    }

    @Test
    void softDeleteProduct_ValidId_ReturnsNoContent() throws ProductNotFoundException {
        // Arrange
        doNothing().when(productServiceInterface).softDeleteById(1L);

        // Act
        ResponseEntity<Void> response = productController.softDeleteProduct(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(productServiceInterface, times(1)).softDeleteById(1L);
    }

    @Test
    void deleteProduct_ValidId_ReturnsNoContent() throws ProductNotFoundException {
        // Arrange
        doNothing().when(productServiceInterface).deleteProduct(1L);

        // Act
        ResponseEntity<Void> response = productController.deleteProduct(1L);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(productServiceInterface, times(1)).deleteProduct(1L);
    }


} 