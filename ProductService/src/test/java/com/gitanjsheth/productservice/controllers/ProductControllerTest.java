package com.gitanjsheth.productservice.controllers;

import com.gitanjsheth.productservice.services.ProductServiceInterface;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductControllerTest {
    @Mock
    private ProductServiceInterface productService;

    @Autowired
    private ProductController productController;

    @Test
    public void testGetSingleProductPositive(){

    }
}