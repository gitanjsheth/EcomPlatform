package com.gitanjsheth.productservice.dtos;

import com.gitanjsheth.productservice.models.Category;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FakeStoreProductDto {
    private Long id;
    private String title;
    private int price;
    private String description;
    private String image;
    private String category;
}
