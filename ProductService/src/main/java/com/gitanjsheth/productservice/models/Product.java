package com.gitanjsheth.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Entity
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE product SET deleted = true WHERE id = ?")
public class Product extends BaseModel {
    @Column(nullable = false)
    @NotBlank(message = "Product title is required")
    @Size(min = 3, max = 100, message = "Product title must be between 3 and 100 characters")
    private String title;
    
    @Column(nullable = false)
    @Min(value = 0, message = "Price must be greater than or equal to 0")
    private Integer price;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    private String imageURL;
    
    @ManyToOne
    @NotNull(message = "Category is required")
    private Category category;
}
