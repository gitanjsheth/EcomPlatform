package com.gitanjsheth.productservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Product extends BaseModel {
    private String title;
    private int price;
    private String description;
    private String imageURL;
    @ManyToOne
    private Category category;
}
