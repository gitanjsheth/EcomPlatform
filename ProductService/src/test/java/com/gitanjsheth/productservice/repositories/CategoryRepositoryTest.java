package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    public void save_NewCategory_PersistsCategory() {
        // Arrange
        Category category = new Category();
        category.setTitle("Electronics");

        // Act
        Category savedCategory = categoryRepository.save(category);

        // Assert
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getTitle()).isEqualTo("Electronics");
        assertThat(savedCategory.isDeleted()).isFalse();
        assertThat(savedCategory.getCreatedAt()).isNotNull();
        assertThat(savedCategory.getLastUpdatedAt()).isNotNull();
    }

    @Test
    public void findById_ExistingCategory_ReturnsCategory() {
        // Arrange
        Category category = new Category();
        category.setTitle("Books");
        Category savedCategory = categoryRepository.save(category);

        // Act
        Optional<Category> foundCategory = categoryRepository.findById(savedCategory.getId());

        // Assert
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getTitle()).isEqualTo("Books");
        assertThat(foundCategory.get().isDeleted()).isFalse();
    }

    @Test
    public void findById_NonExistentCategory_ReturnsEmpty() {
        // Act
        Optional<Category> foundCategory = categoryRepository.findById(999L);

        // Assert
        assertThat(foundCategory).isEmpty();
    }

    @Test
    public void findByTitle_ExistingTitle_ReturnsCategory() {
        // Arrange
        Category category = new Category();
        category.setTitle("Clothing");
        categoryRepository.save(category);

        // Act
        Optional<Category> foundCategory = categoryRepository.findByTitle("Clothing");

        // Assert
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().getTitle()).isEqualTo("Clothing");
        assertThat(foundCategory.get().isDeleted()).isFalse();
    }

    @Test
    public void findByTitle_NonExistentTitle_ReturnsEmpty() {
        // Act
        Optional<Category> foundCategory = categoryRepository.findByTitle("NonExistent");

        // Assert
        assertThat(foundCategory).isEmpty();
    }

    @Test
    public void findByTitle_SoftDeletedCategory_ReturnsEmpty() {
        // Arrange
        Category category = new Category();
        category.setTitle("Sports");
        Category savedCategory = categoryRepository.save(category);

        // Soft delete the category using @SQLDelete annotation
        categoryRepository.deleteById(savedCategory.getId());

        // Act
        Optional<Category> foundCategory = categoryRepository.findByTitle("Sports");

        // Assert
        assertThat(foundCategory).isEmpty();
    }

    @Test
    public void deleteById_WithSQLDeleteAnnotation_MarksAsDeleted() {
        // Arrange
        Category category = new Category();
        category.setTitle("Home & Garden");
        Category savedCategory = categoryRepository.save(category);

        // Act - deleteById now triggers soft delete via @SQLDelete annotation
        categoryRepository.deleteById(savedCategory.getId());
        testEntityManager.flush(); // Force database synchronization
        testEntityManager.clear(); // Clear the persistence context to force a fresh read

        // Assert
        Optional<Category> foundCategory = categoryRepository.findDeletedById(savedCategory.getId());
        assertThat(foundCategory).isPresent();
        assertThat(foundCategory.get().isDeleted()).isTrue();
    }

    @Test
    public void hardDeleteById_ExistingCategory_RemovesFromDatabase() {
        // Arrange
        Category category = new Category();
        category.setTitle("Automotive");
        Category savedCategory = categoryRepository.save(category);
        Long categoryId = savedCategory.getId();

        // Act - Use hard delete to actually remove from database
        categoryRepository.hardDeleteById(categoryId);
        testEntityManager.flush(); // Force database synchronization
        testEntityManager.clear(); // Clear the persistence context to force a fresh read

        // Assert
        Optional<Category> foundCategory = categoryRepository.findById(categoryId);
        assertThat(foundCategory).isEmpty();
    }

    @Test
    public void findAll_ReturnsOnlyNonDeletedCategories() {
        // Arrange
        Category category1 = new Category();
        category1.setTitle("Technology");
        Category savedCategory1 = categoryRepository.save(category1);

        Category category2 = new Category();
        category2.setTitle("Food");
        categoryRepository.save(category2);

        // Soft delete one category using @SQLDelete annotation
        categoryRepository.deleteById(savedCategory1.getId());

        // Act
        List<Category> allCategories = categoryRepository.findAll();

        // Assert
        assertThat(allCategories).hasSize(1);
        assertThat(allCategories.get(0).getTitle()).isEqualTo("Food");
        assertThat(allCategories.get(0).isDeleted()).isFalse();
    }

    @Test
    public void save_UpdateExistingCategory_UpdatesSuccessfully() throws InterruptedException {
        // Arrange
        Category category = new Category();
        category.setTitle("Original Title");
        Category savedCategory = categoryRepository.save(category);

        // Add a small delay to ensure different timestamps
        Thread.sleep(10);

        // Act
        savedCategory.setTitle("Updated Title");
        Category updatedCategory = categoryRepository.save(savedCategory);

        // Assert
        assertThat(updatedCategory.getId()).isEqualTo(savedCategory.getId());
        assertThat(updatedCategory.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedCategory.getLastUpdatedAt()).isAfterOrEqualTo(updatedCategory.getCreatedAt());
    }

    @Test
    public void findByTitle_CaseSensitive_ReturnsEmpty() {
        // Arrange
        Category category = new Category();
        category.setTitle("electronics");
        categoryRepository.save(category);

        // Act
        Optional<Category> foundCategory = categoryRepository.findByTitle("Electronics");

        // Assert
        assertThat(foundCategory).isEmpty();
    }
} 