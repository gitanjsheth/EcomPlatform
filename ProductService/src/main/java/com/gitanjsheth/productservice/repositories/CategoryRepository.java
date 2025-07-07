package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Override findById to exclude soft-deleted categories
    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.deleted = false")
    Optional<Category> findById(@Param("id") Long categoryId);

    // Override findAll to exclude soft-deleted categories
    @Query("SELECT c FROM Category c WHERE c.deleted = false")
    List<Category> findAll();

    @Query("SELECT c FROM Category c WHERE c.title = :title AND c.deleted = false")
    Optional<Category> findByTitle(@Param("title") String title);

    @Modifying
    @Transactional
    @Query("UPDATE Category c SET c.deleted = true WHERE c.id = :id")
    void softDeleteById(@Param("id") Long categoryId);

    // Additional methods for soft delete functionality
    @Query("SELECT c FROM Category c WHERE c.deleted = true")
    List<Category> findAllDeleted();

    @Query("SELECT c FROM Category c WHERE c.id = :id AND c.deleted = true")
    Optional<Category> findDeletedById(@Param("id") Long categoryId);
}