package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.ProductSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSearchRepository extends ElasticsearchRepository<ProductSearchDocument, String> {
    List<ProductSearchDocument> findByTitleContainingIgnoreCase(String title);
    List<ProductSearchDocument> findByCategory(String category);
}


