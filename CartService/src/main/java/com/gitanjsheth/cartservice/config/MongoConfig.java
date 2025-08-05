package com.gitanjsheth.cartservice.config;

import com.gitanjsheth.cartservice.models.Cart;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import jakarta.annotation.PostConstruct;

@Configuration
public class MongoConfig {
    
    private final MongoTemplate mongoTemplate;
    
    public MongoConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
    
    @PostConstruct
    public void createIndexes() {
        // Create TTL index on expiresAt field for automatic document expiration
        IndexOperations indexOps = mongoTemplate.indexOps(Cart.class);
        
        // TTL index that expires documents based on the expiresAt field
        Index ttlIndex = new Index().on("expiresAt", org.springframework.data.domain.Sort.Direction.ASC)
                                   .expire(0); // Expire immediately when expiresAt time is reached
        
        indexOps.ensureIndex(ttlIndex);
        
        // Additional useful indexes
        indexOps.ensureIndex(new Index().on("userId", org.springframework.data.domain.Sort.Direction.ASC));
        indexOps.ensureIndex(new Index().on("sessionId", org.springframework.data.domain.Sort.Direction.ASC));
        indexOps.ensureIndex(new Index().on("status", org.springframework.data.domain.Sort.Direction.ASC));
        indexOps.ensureIndex(new Index().on("updatedAt", org.springframework.data.domain.Sort.Direction.DESC));
    }
}