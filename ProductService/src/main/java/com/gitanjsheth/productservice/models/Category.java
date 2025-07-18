package com.gitanjsheth.productservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Getter
@Setter
@Entity
@Where(clause = "deleted = false")
@SQLDelete(sql = "UPDATE category SET deleted = true WHERE id = ?")
public class Category extends BaseModel {
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Category title is required")
    @Size(min = 2, max = 50, message = "Category title must be between 2 and 50 characters")
    private String title;
}
