package com.gitanjsheth.productservice.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Product extends BaseModel {
    private String title;
    private int price;
    private String description;
    private String imageURL;
    private Category category;
}
