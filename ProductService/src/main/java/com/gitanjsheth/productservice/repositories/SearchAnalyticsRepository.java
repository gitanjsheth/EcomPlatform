package com.gitanjsheth.productservice.repositories;

import com.gitanjsheth.productservice.models.SearchAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchAnalyticsRepository extends JpaRepository<SearchAnalytics, Long> {
}


