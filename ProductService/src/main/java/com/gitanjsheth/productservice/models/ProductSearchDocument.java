package com.gitanjsheth.productservice.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@Document(indexName = "products")
public class ProductSearchDocument {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long productId;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String title;

    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Integer)
    private Integer price;

    @Field(type = FieldType.Integer)
    private Integer availableQuantity;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;
}


