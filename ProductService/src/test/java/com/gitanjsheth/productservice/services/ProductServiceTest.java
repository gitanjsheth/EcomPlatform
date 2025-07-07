package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.repositories.CategoryRepository;
import com.gitanjsheth.productservice.repositories.ProductRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private SelfProductService productService;

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
    void getSingleProduct_ExistingProduct_ReturnsProduct() throws ProductNotFoundException {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Product result = productService.getSingleProduct(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        assertEquals(testProduct.getTitle(), result.getTitle());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getSingleProduct_NonExistingProduct_ThrowsException() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, 
            () -> productService.getSingleProduct(1L));
        assertEquals(1L, exception.getProductId());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getAllProducts_ReturnsListOfProducts() {
        // Arrange
        List<Product> products = Arrays.asList(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
        verify(productRepository, times(1)).findAll();
    }

    @Test
    void createProduct_WithExistingCategory_ReturnsCreatedProduct() throws CategoryNotFoundException {
        // Arrange
        when(categoryRepository.findByTitle("Electronics")).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        verify(categoryRepository, times(1)).findByTitle("Electronics");
        verify(productRepository, times(1)).save(testProduct);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void createProduct_WithNewCategory_CreatesNewCategoryAndProduct() throws CategoryNotFoundException {
        // Arrange
        when(categoryRepository.findByTitle("Electronics")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.createProduct(testProduct);

        // Assert
        assertNotNull(result);
        assertEquals(testProduct.getId(), result.getId());
        verify(categoryRepository, times(1)).findByTitle("Electronics");
        verify(categoryRepository, times(1)).save(testCategory);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void createProduct_WithNullCategory_ThrowsException() {
        // Arrange
        testProduct.setCategory(null);

        // Act & Assert
        CategoryNotFoundException exception = assertThrows(CategoryNotFoundException.class, 
            () -> productService.createProduct(testProduct));
        assertEquals("Need a category to add product.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_ExistingProduct_UpdatesAndReturnsProduct() throws ProductNotFoundException, CategoryNotFoundException {
        // Arrange
        Product updateData = new Product();
        updateData.setTitle("Updated Title");
        updateData.setPrice(150);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        Product result = productService.updateProduct(1L, updateData);

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(testProduct);
    }

    @Test
    void updateProduct_NonExistingProduct_ThrowsException() {
        // Arrange
        Product updateData = new Product();
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ProductNotFoundException exception = assertThrows(ProductNotFoundException.class, 
            () -> productService.updateProduct(1L, updateData));
        assertEquals(1L, exception.getProductId());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_CallsRepository() {
        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).deleteById(1L);
    }

    @Test
    void softDeleteById_CallsRepository() {
        // Act
        productService.softDeleteById(1L);

        // Assert
        verify(productRepository, times(1)).softDeleteById(1L);
    }

    @Test
    void testUpdateProductPartialData() throws ProductNotFoundException, CategoryNotFoundException {
        // Create a product
        Product originalProduct = new Product();
        originalProduct.setTitle("Original Title");
        originalProduct.setDescription("Original Description");
        originalProduct.setPrice(100);
        originalProduct.setImageURL("http://original.com/image.jpg");
        originalProduct.setCategory(testCategory);
        
        when(productRepository.findById(1L)).thenReturn(Optional.of(originalProduct));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // Create partial update - only title and price
        Product partialUpdate = new Product();
        partialUpdate.setTitle("Updated Title");
        partialUpdate.setPrice(200);
        // Note: description, imageURL, and category are null
        
        // Update the product
        Product updatedProduct = productService.updateProduct(1L, partialUpdate);
        
        // Verify only specified fields were updated, others preserved
        assertEquals("Updated Title", updatedProduct.getTitle());
        assertEquals("Original Description", updatedProduct.getDescription()); // preserved
        assertEquals(Integer.valueOf(200), updatedProduct.getPrice());
        assertEquals("http://original.com/image.jpg", updatedProduct.getImageURL()); // preserved
        assertEquals(testCategory, updatedProduct.getCategory()); // preserved
    }
} 