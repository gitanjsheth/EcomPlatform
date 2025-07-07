package com.gitanjsheth.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Category extends BaseModel {
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Category title is required")
    @Size(min = 2, max = 50, message = "Category title must be between 2 and 50 characters")
    private String title;
}
