package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.models.ProductSearchDocument;
import com.gitanjsheth.productservice.services.SearchService;
import com.gitanjsheth.productservice.services.SearchAnalyticsService;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/search")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080", "http://localhost:8081"})
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final SearchAnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<Page<ProductSearchDocument>> search(@RequestParam("q") String query,
                                                             @RequestParam(value = "page", defaultValue = "0") int page,
                                                             @RequestParam(value = "size", defaultValue = "10") int size,
                                                             HttpServletRequest request) {
        long start = System.currentTimeMillis();
        Page<ProductSearchDocument> result = searchService.search(query, page, size);
        long elapsed = System.currentTimeMillis() - start;
        analyticsService.record(query, elapsed, (int) result.getTotalElements(),
                request.getHeader("User-Agent"), request.getRemoteAddr());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<String>> autocomplete(@RequestParam("q") String prefix,
                                                     @RequestParam(value = "size", defaultValue = "10") int size,
                                                     HttpServletRequest request) {
        long start = System.currentTimeMillis();
        List<String> suggestions = searchService.autocomplete(prefix, size);
        long elapsed = System.currentTimeMillis() - start;
        analyticsService.record(prefix, elapsed, suggestions.size(),
                request.getHeader("User-Agent"), request.getRemoteAddr());
        return ResponseEntity.ok(suggestions);
    }
}


