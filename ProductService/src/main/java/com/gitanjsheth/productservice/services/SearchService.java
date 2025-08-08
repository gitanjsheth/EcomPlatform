package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.models.Product;
import com.gitanjsheth.productservice.models.ProductSearchDocument;
import com.gitanjsheth.productservice.repositories.ProductSearchRepository;
import com.gitanjsheth.productservice.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

// query builders provided via NativeQuery lambdas

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ProductSearchRepository productSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductRepository productRepository;

    public void indexProduct(Product product) {
        if (product == null) return;
        ProductSearchDocument doc = toDocument(product);
        productSearchRepository.save(doc);
    }

    public void deleteProductIndex(Long productId) {
        if (productId == null) return;
        Query query = NativeQuery.builder()
                .withQuery(q -> q.term(t -> t.field("productId").value(productId)))
                .build();
        SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(query, ProductSearchDocument.class);
        hits.forEach(hit -> productSearchRepository.deleteById(hit.getId()));
    }

    public long reindexAll() {
        Iterable<Product> products = productRepository.findAll();
        long count = 0;
        for (Product product : products) {
            indexProduct(product);
            count++;
        }
        return count;
    }

    public Page<ProductSearchDocument> search(String queryText, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Query esQuery = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b
                        .should(s -> s.match(m -> m.field("title").query(queryText)))
                        .should(s -> s.match(m -> m.field("description").query(queryText)))
                ))
                .withPageable(pageable)
                .build();
        SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(esQuery, ProductSearchDocument.class);
        List<ProductSearchDocument> results = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        return new PageImpl<>(results, pageable, hits.getTotalHits());
    }

    public List<String> autocomplete(String prefix, int size) {
        Query esQuery = NativeQuery.builder()
                .withQuery(q -> q.prefix(p -> p.field("title").value(prefix)))
                .withPageable(PageRequest.of(0, size))
                .build();
        SearchHits<ProductSearchDocument> hits = elasticsearchOperations.search(esQuery, ProductSearchDocument.class);
        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent().getTitle())
                .distinct()
                .collect(Collectors.toList());
    }

    private ProductSearchDocument toDocument(Product product) {
        ProductSearchDocument doc = new ProductSearchDocument();
        doc.setProductId(product.getId());
        doc.setTitle(product.getTitle());
        doc.setDescription(product.getDescription());
        doc.setCategory(product.getCategory() != null ? product.getCategory().getTitle() : null);
        doc.setPrice(product.getPrice());
        doc.setAvailableQuantity(product.getAvailableQuantity());
        doc.setIsActive(Boolean.TRUE.equals(product.getIsActive()));
        return doc;
    }
}


