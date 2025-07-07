package com.gitanjsheth.productservice.controllerAdvice;

import com.gitanjsheth.productservice.dtos.ExceptionDto;
import com.gitanjsheth.productservice.dtos.ProductNotFoundExceptionDto;
import com.gitanjsheth.productservice.dtos.CategoryNotFoundExceptionDto;
import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

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

        exception.printStackTrace();
        prodNotFoundExpDto.setProductId(exception.getProductId());
        prodNotFoundExpDto.setMessage("Product not found!");
        prodNotFoundExpDto.setResolution("Try again with correct product id");

        return new ResponseEntity<>(prodNotFoundExpDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<CategoryNotFoundExceptionDto> handleCategoryNotFoundException(CategoryNotFoundException exception) {
        CategoryNotFoundExceptionDto categoryNotFoundExpDto = new CategoryNotFoundExceptionDto();

        exception.printStackTrace();
        categoryNotFoundExpDto.setCategoryId(exception.getCategoryId());
        categoryNotFoundExpDto.setMessage("Category not found!");
        categoryNotFoundExpDto.setResolution("Try again with correct category id");

        return new ResponseEntity<>(categoryNotFoundExpDto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
