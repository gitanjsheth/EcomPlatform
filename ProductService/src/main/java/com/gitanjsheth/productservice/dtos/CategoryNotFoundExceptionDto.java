package com.gitanjsheth.productservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryNotFoundExceptionDto {
    private Long categoryId;
    private String message;
    private String resolution;
}