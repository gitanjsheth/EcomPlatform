package com.gitanjsheth.productservice.configuration;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.time.Duration;

/**
 * Elasticsearch Configuration for Product Service
 * Provides explicit configuration for Elasticsearch integration including:
 * - Connection settings and timeouts
 * - Index management
 * - Custom client configurations
 * - Repository enablement
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.gitanjsheth.productservice.repositories")
public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.connection-timeout:5000}")
    private int connectionTimeout;

    @Value("${spring.elasticsearch.socket-timeout:60000}")
    private int socketTimeout;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Value("${spring.elasticsearch.index-name:products}")
    private String indexName;

    @Value("${spring.elasticsearch.number-of-shards:1}")
    private int numberOfShards;

    @Value("${spring.elasticsearch.number-of-replicas:0}")
    private int numberOfReplicas;

    @Override
    @Bean
    public RestHighLevelClient elasticsearchClient() {
        ClientConfiguration.MaybeSecureClientConfigurationBuilder builder = ClientConfiguration.builder()
                .connectedTo(extractHostAndPort(elasticsearchUris))
                .withConnectTimeout(Duration.ofMillis(connectionTimeout))
                .withSocketTimeout(Duration.ofMillis(socketTimeout));

        // Add authentication if credentials are provided
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
            builder.withBasicAuth(username, password);
        }

        // Add additional configurations
        ClientConfiguration clientConfiguration = builder
                .withClientConfigurer(RestClients.RestClientConfigurationCallback.from(clientBuilder -> {
                    clientBuilder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                            .setConnectTimeout(connectionTimeout)
                            .setSocketTimeout(socketTimeout));
                    return clientBuilder;
                }))
                .build();

        return RestClients.create(clientConfiguration).rest();
    }

    @Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchRestTemplate(elasticsearchClient());
    }

    /**
     * Extract host and port from URI string
     * Supports both single URI and comma-separated URIs
     */
    private String extractHostAndPort(String uris) {
        if (uris.contains(",")) {
            // Multiple URIs - take the first one for now
            uris = uris.split(",")[0];
        }
        
        // Remove protocol if present
        if (uris.startsWith("http://")) {
            uris = uris.substring(7);
        } else if (uris.startsWith("https://")) {
            uris = uris.substring(8);
        }
        
        return uris;
    }

    /**
     * Get configured index name
     */
    public String getIndexName() {
        return indexName;
    }

    /**
     * Get configured number of shards
     */
    public int getNumberOfShards() {
        return numberOfShards;
    }

    /**
     * Get configured number of replicas
     */
    public int getNumberOfReplicas() {
        return numberOfReplicas;
    }
}
