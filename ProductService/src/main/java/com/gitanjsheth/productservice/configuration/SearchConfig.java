package com.gitanjsheth.productservice.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.gitanjsheth.productservice.repositories")
public class SearchConfig {
}


