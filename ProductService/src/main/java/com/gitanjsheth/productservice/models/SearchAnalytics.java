package com.gitanjsheth.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SearchAnalytics extends BaseModel {

    @Column(nullable = false)
    private String query;

    @Column(nullable = false)
    private Long executionTimeMs;

    @Column(nullable = false)
    private Integer resultCount;

    private String userAgent;
    private String ipAddress;
}


