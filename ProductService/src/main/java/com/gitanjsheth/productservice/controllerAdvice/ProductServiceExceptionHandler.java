package com.gitanjsheth.productservice.controllerAdvice;

import com.gitanjsheth.productservice.dtos.ExceptionDto;
import com.gitanjsheth.productservice.dtos.ProductNotFoundExceptionDto;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ProductServiceExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ExceptionDto> handleRuntimeException() {

        ExceptionDto exceptionDto = new ExceptionDto();
        exceptionDto.setMessage("Something went wrong!");
        exceptionDto.setDetails("Insufficient Payment.");

        return new ResponseEntity<>(exceptionDto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProductNotFoundExceptionDto> handleProductNotFoundException(ProductNotFoundException exception) {
        ProductNotFoundExceptionDto prodNotFoundExpDto = new ProductNotFoundExceptionDto();

        prodNotFoundExpDto.setProductId(exception.getProductId());
        prodNotFoundExpDto.setMessage("Product not found!");
        prodNotFoundExpDto.setResolution("Try again with correct product id");

        return new ResponseEntity<>(prodNotFoundExpDto, HttpStatus.NOT_FOUND);
    }
}
