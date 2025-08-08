package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.services.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/search")
@RequiredArgsConstructor
public class SearchAdminController {
    private final SearchService searchService;

    @PostMapping("/reindex")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> reindexAll() {
        long count = searchService.reindexAll();
        return ResponseEntity.ok(Map.of("indexed", count));
    }
}


