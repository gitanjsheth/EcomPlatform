package com.gitanjsheth.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Product extends BaseModel {
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private int price;
    private String description;
    private String imageURL;
    @ManyToOne
    private Category category;
}
