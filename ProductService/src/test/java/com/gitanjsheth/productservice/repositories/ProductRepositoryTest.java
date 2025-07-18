package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setTitle("Electronics");
        testCategory = entityManager.persistAndFlush(testCategory);

        testProduct = new Product();
        testProduct.setTitle("Test Product");
        testProduct.setPrice(100);
        testProduct.setDescription("Test Description");
        testProduct.setImageURL("http://test.com/image.jpg");
        testProduct.setCategory(testCategory);
        testProduct = entityManager.persistAndFlush(testProduct);
    }

    @Test
    void findById_ExistingProduct_ReturnsProduct() {
        // Act
        Optional<Product> result = productRepository.findById(testProduct.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProduct.getId(), result.get().getId());
        assertEquals(testProduct.getTitle(), result.get().getTitle());
    }

    @Test
    void findById_SoftDeletedProduct_ReturnsEmpty() {
        // Arrange - Use soft delete via @SQLDelete annotation
        productRepository.deleteById(testProduct.getId());
        entityManager.flush();
        entityManager.clear(); // Clear the cache to ensure fresh load

        // Act
        Optional<Product> result = productRepository.findById(testProduct.getId());

        // Assert
        assertFalse(result.isPresent()); // Should not be found due to @Where annotation
    }

    @Test
    void findAll_ExcludesSoftDeletedProducts() {
        // Arrange
        Product deletedProduct = new Product();
        deletedProduct.setTitle("Deleted Product");
        deletedProduct.setPrice(200);
        deletedProduct.setCategory(testCategory);
        deletedProduct.setDeleted(true);
        entityManager.persistAndFlush(deletedProduct);

        // Act
        List<Product> result = productRepository.findAll();

        // Assert
        assertFalse(result.isEmpty());
        assertEquals(1, result.size()); // Should only return non-deleted product
        assertEquals(testProduct.getId(), result.get(0).getId());
    }

    @Test
    void findByTitle_ReturnsMatchingProduct() {
        // Act
        Optional<Product> result = productRepository.findByTitle("Test Product");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProduct.getId(), result.get().getId());
    }

    @Test
    void findByPriceBetween_ReturnsProductsInRange() {
        // Arrange
        Product expensiveProduct = new Product();
        expensiveProduct.setTitle("Expensive Product");
        expensiveProduct.setPrice(500);
        expensiveProduct.setCategory(testCategory);
        entityManager.persistAndFlush(expensiveProduct);

        // Act
        List<Product> result = productRepository.findByPriceBetween(50, 150);

        // Assert
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
    }

    @Test
    void findByCategory_ReturnsProductsInCategory() {
        // Arrange
        Product anotherProduct = new Product();
        anotherProduct.setTitle("Another Product");
        anotherProduct.setPrice(150);
        anotherProduct.setCategory(testCategory);
        entityManager.persistAndFlush(anotherProduct);

        // Act
        List<Product> result = productRepository.findByCategory(testCategory);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void findByCategory_Title_ReturnsProductsInCategoryByTitle() {
        // Act
        List<Product> result = productRepository.findByCategory_Title("Electronics");

        // Assert
        assertEquals(1, result.size());
        assertEquals(testProduct.getId(), result.get(0).getId());
    }

    @Test
    void deleteById_WithSQLDeleteAnnotation_MarksProductAsDeleted() {
        // Act - deleteById now triggers soft delete via @SQLDelete annotation
        productRepository.deleteById(testProduct.getId());
        entityManager.flush();
        entityManager.clear(); // Clear the cache to ensure fresh load

        // Assert
        Optional<Product> result = productRepository.findById(testProduct.getId());
        assertFalse(result.isPresent()); // Should not be found by regular findById due to @Where annotation

        // But should be found by findDeletedById
        Optional<Product> deletedResult = productRepository.findDeletedById(testProduct.getId());
        assertTrue(deletedResult.isPresent());
        assertTrue(deletedResult.get().isDeleted());
    }

    @Test
    void findAllDeleted_ReturnsOnlyDeletedProducts() {
        // Arrange
        productRepository.deleteById(testProduct.getId()); // Now uses @SQLDelete for soft delete
        entityManager.flush();
        entityManager.clear(); // Clear the cache to ensure fresh load

        Product anotherProduct = new Product();
        anotherProduct.setTitle("Active Product");
        anotherProduct.setPrice(150);
        anotherProduct.setCategory(testCategory);
        entityManager.persistAndFlush(anotherProduct);

        // Act
        List<Product> deletedProducts = productRepository.findAllDeleted();

        // Assert
        assertEquals(1, deletedProducts.size());
        assertEquals(testProduct.getId(), deletedProducts.get(0).getId());
        assertTrue(deletedProducts.get(0).isDeleted());
    }

    @Test
    void findDeletedById_ReturnsDeletedProduct() {
        // Arrange
        productRepository.deleteById(testProduct.getId()); // Now uses @SQLDelete for soft delete
        entityManager.flush();
        entityManager.clear(); // Clear the cache to ensure fresh load

        // Act
        Optional<Product> result = productRepository.findDeletedById(testProduct.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testProduct.getId(), result.get().getId());
        assertTrue(result.get().isDeleted());
    }

    @Test
    void findDeletedById_ActiveProduct_ReturnsEmpty() {
        // Act
        Optional<Product> result = productRepository.findDeletedById(testProduct.getId());

        // Assert
        assertFalse(result.isPresent()); // Active product should not be found
    }

    @Test
    void save_NewProduct_PersistsProduct() {
        // Arrange
        Product newProduct = new Product();
        newProduct.setTitle("New Product");
        newProduct.setPrice(300);
        newProduct.setCategory(testCategory);

        // Act
        Product savedProduct = productRepository.save(newProduct);

        // Assert
        assertNotNull(savedProduct.getId());
        assertEquals("New Product", savedProduct.getTitle());
        assertEquals(300, savedProduct.getPrice());
        
        // Verify it can be found
        Optional<Product> found = productRepository.findById(savedProduct.getId());
        assertTrue(found.isPresent());
    }

    @Test
    void hardDeleteById_RemovesProductFromDatabase() {
        // Arrange
        Long productId = testProduct.getId();

        // Act - Use hard delete to actually remove from database
        productRepository.hardDeleteById(productId);
        entityManager.flush();
        entityManager.clear(); // Clear the cache to ensure fresh load

        // Assert
        Optional<Product> result = productRepository.findById(productId);
        assertFalse(result.isPresent());
        
        // Should also not be found in deleted products
        Optional<Product> deletedResult = productRepository.findDeletedById(productId);
        assertFalse(deletedResult.isPresent());
    }

    @Test
    void findProductWithGivenId_ReturnsProductRegardlessOfDeletedStatus() {
        // Arrange - Test with active product
        Product activeResult = productRepository.findProductWithGivenId(testProduct.getId());
        assertNotNull(activeResult);

        // Arrange - Test with deleted product
        productRepository.deleteById(testProduct.getId()); // Now uses @SQLDelete for soft delete
        entityManager.flush();

        // Act
        Product deletedResult = productRepository.findProductWithGivenId(testProduct.getId());

        // Assert
        assertNotNull(deletedResult);
        assertEquals(testProduct.getId(), deletedResult.getId());
    }
} 