package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.dtos.FakeStoreProductDto;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class FakeStoreProductService implements ProductServiceInterface{
    //Note: This service class will implement all the APIs using FakeStore.

    private RestTemplate restTemplate;

    public FakeStoreProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getSingleProduct(Long productId) {
        ResponseEntity<FakeStoreProductDto> fakeStoreProductDtoResponse = restTemplate
                .getForEntity("https://fakestoreapi.com/products/" + productId, FakeStoreProductDto.class);

        FakeStoreProductDto fakeStoreProductDto = fakeStoreProductDtoResponse.getBody();

        return convertFakeStoreProductDtoToProduct(fakeStoreProductDto);
    }

    @Override
    public List<Product> getAllProducts() {
        ResponseEntity<FakeStoreProductDto[]> fakeStoreProductDtoResponse = restTemplate
                .getForEntity("https://fakestoreapi.com/products/", FakeStoreProductDto[].class);

        FakeStoreProductDto[] fakeStoreProductDtoList = fakeStoreProductDtoResponse.getBody();

        List<Product> products = new ArrayList<>();

        assert fakeStoreProductDtoList != null;
        for (FakeStoreProductDto fakeStoreProductDto : fakeStoreProductDtoList) {
            products.add(convertFakeStoreProductDtoToProduct(fakeStoreProductDto));
        }
        return products;
    }

    @Override
    public Product createProduct(Product product) {
        return null;
    }

    @Override
    public Product updateProduct(Long productId, Product product) {
        return null;
    }

    @Override
    public Product changeProduct(Long productId, Product product) {
        return null;
    }

    @Override
    public Boolean deleteProduct(Long productId) {
        return null;
    }

    private static Product convertFakeStoreProductDtoToProduct(FakeStoreProductDto fakeStoreProductDto) {
        if (fakeStoreProductDto == null) {
            return null;
        }
        Product product = new Product();
        product.setId(fakeStoreProductDto.getId());
        product.setTitle(fakeStoreProductDto.getTitle());
        product.setDescription(fakeStoreProductDto.getDescription());
        product.setPrice(fakeStoreProductDto.getPrice());
        product.setImageURL(fakeStoreProductDto.getImage());

        Category category = new Category();
        category.setTitle(fakeStoreProductDto.getCategory());
        product.setCategory(category);

        return product;
    }
}
