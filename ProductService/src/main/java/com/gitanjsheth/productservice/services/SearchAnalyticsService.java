package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.models.SearchAnalytics;
import com.gitanjsheth.productservice.repositories.SearchAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SearchAnalyticsService {
    private final SearchAnalyticsRepository repository;

    public void record(String query, long executionTimeMs, int resultCount, String userAgent, String ipAddress) {
        SearchAnalytics analytics = new SearchAnalytics();
        analytics.setQuery(query);
        analytics.setExecutionTimeMs(executionTimeMs);
        analytics.setResultCount(resultCount);
        analytics.setUserAgent(userAgent);
        analytics.setIpAddress(ipAddress);
        repository.save(analytics);
    }
}


