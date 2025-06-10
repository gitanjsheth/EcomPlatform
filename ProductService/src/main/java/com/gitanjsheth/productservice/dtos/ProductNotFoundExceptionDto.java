package com.gitanjsheth.productservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductNotFoundExceptionDto {
    private long productId;
    private String message;
    private String resolution;
}
