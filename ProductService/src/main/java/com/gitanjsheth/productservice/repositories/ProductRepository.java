package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gitanjsheth.productservice.models.Product;

import java.util.Optional;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Override
    Optional<Product> findById(Long productId);

    Optional<Product> findByTitle(String title);

    List<Product> findByPriceBetween(int priceAfter, int priceBefore);

    List<Product> findByCategory(Category category);

    List<Product> findByCategory_Title(String categoryTitle);
}
