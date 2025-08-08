package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.models.SearchAnalytics;
import com.gitanjsheth.productservice.repositories.SearchAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/search-analytics")
@RequiredArgsConstructor
public class SearchAnalyticsController {

    private final SearchAnalyticsRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SearchAnalytics>> list() {
        return ResponseEntity.ok(repository.findAll());
    }
}


