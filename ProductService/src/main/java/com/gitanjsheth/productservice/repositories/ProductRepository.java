package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.gitanjsheth.productservice.models.Product;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // findById and findAll now automatically exclude soft-deleted products via @Where annotation

    // These queries now automatically exclude soft-deleted products via @Where annotation
    Optional<Product> findByTitle(String title);
    
    List<Product> findByPriceBetween(int priceAfter, int priceBefore);
    
    List<Product> findByCategory(Category category);
    
    List<Product> findByCategory_Title(String categoryTitle);

    // softDeleteById is now handled by @SQLDelete annotation - just use deleteById
    
    // For hard delete (actual removal from database) - bypass @SQLDelete annotation
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM product WHERE id = ?1", nativeQuery = true)
    void hardDeleteById(Long productId);

    @Query(value = "SELECT * FROM product WHERE id = ?1", nativeQuery = true)
    Product findProductWithGivenId(Long productId);

    // Additional methods for soft delete functionality - bypassing @Where annotation
    @Query(value = "SELECT * FROM product WHERE deleted = true", nativeQuery = true)
    List<Product> findAllDeleted();

    @Query(value = "SELECT * FROM product WHERE id = ?1 AND deleted = true", nativeQuery = true)
    Optional<Product> findDeletedById(Long productId);

    // Inventory management queries
    List<Product> findByIsOutOfStockTrue();
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.isActive = true")
    List<Product> findLowStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND (p.isOutOfStock = false OR p.allowBackorder = true)")
    List<Product> findAvailableProducts();
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.showWhenOutOfStock = true")
    List<Product> findDisplayableProducts();

}
