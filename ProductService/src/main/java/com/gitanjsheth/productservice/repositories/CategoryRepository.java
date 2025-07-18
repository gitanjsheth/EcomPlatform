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

    // findById, findAll, and findByTitle now automatically exclude soft-deleted categories via @Where annotation
    Optional<Category> findByTitle(String title);
    
    // For hard delete (actual removal from database) - bypass @SQLDelete annotation
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM category WHERE id = ?1", nativeQuery = true)
    void hardDeleteById(Long categoryId);

    // Additional methods for soft delete functionality - bypassing @Where annotation
    @Query(value = "SELECT * FROM category WHERE deleted = true", nativeQuery = true)
    List<Category> findAllDeleted();

    @Query(value = "SELECT * FROM category WHERE id = ?1 AND deleted = true", nativeQuery = true)
    Optional<Category> findDeletedById(Long categoryId);
}