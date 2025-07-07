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

    // Override findById to exclude soft-deleted products
    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = false")
    Optional<Product> findById(@Param("id") Long productId);

    // Override findAll to exclude soft-deleted products
    @Query("SELECT p FROM Product p WHERE p.deleted = false")
    List<Product> findAll();

    @Query("SELECT p FROM Product p WHERE p.title = :title AND p.deleted = false")
    Optional<Product> findByTitle(@Param("title") String title);

    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :priceAfter AND :priceBefore AND p.deleted = false")
    List<Product> findByPriceBetween(@Param("priceAfter") int priceAfter, @Param("priceBefore") int priceBefore);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.deleted = false")
    List<Product> findByCategory(@Param("category") Category category);

    @Query("SELECT p FROM Product p WHERE p.category.title = :categoryTitle AND p.deleted = false")
    List<Product> findByCategory_Title(@Param("categoryTitle") String categoryTitle);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deleted = true WHERE p.id = :id")
    void softDeleteById(@Param("id") Long productId);

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product findProductWithGivenId(@Param("id") Long productId);

    // Additional methods for soft delete functionality
    @Query("SELECT p FROM Product p WHERE p.deleted = true")
    List<Product> findAllDeleted();

    @Query("SELECT p FROM Product p WHERE p.id = :id AND p.deleted = true")
    Optional<Product> findDeletedById(@Param("id") Long productId);

}
