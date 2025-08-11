package com.gitanjsheth.productservice.services;

import com.gitanjsheth.productservice.dtos.FakeStoreProductDto;
import com.gitanjsheth.productservice.exceptions.CategoryNotFoundException;
import com.gitanjsheth.productservice.exceptions.ProductNotFoundException;
import com.gitanjsheth.productservice.models.Category;
import com.gitanjsheth.productservice.models.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service ("fakeStoreProductService")
public class FakeStoreProductService implements ProductServiceInterface{
    //Note: This service class will implement all the APIs using FakeStore.

    private RestTemplate restTemplate;

    public FakeStoreProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Product getSingleProduct(Long productId) throws ProductNotFoundException {
        ResponseEntity<FakeStoreProductDto> fakeStoreProductDtoResponse = restTemplate
                .getForEntity("https://fakestoreapi.com/products/" + productId, FakeStoreProductDto.class);

        FakeStoreProductDto fakeStoreProductDto = fakeStoreProductDtoResponse.getBody();

        if (fakeStoreProductDto == null) {
            //Wrong product ID
            throw new ProductNotFoundException(productId);
        }

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
    public Product createProduct(Product product) throws CategoryNotFoundException {
        return null;
    }

    @Override
    public Product updateProduct(Long productId, Product product) throws ProductNotFoundException, CategoryNotFoundException {
        return null;
    }

    @Override
    public void deleteProduct(Long productId) {}

    @Override
    public void softDeleteById(Long productId) {}

    @Override
    public List<Product> getProductsByCategory(Long categoryId) throws CategoryNotFoundException {
        // For FakeStore, we'll get all products and filter by category
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getCategory() != null && 
                        product.getCategory().getId() != null && 
                        product.getCategory().getId().equals(categoryId))
                .toList();
    }

    @Override
    public List<Product> getProductsByCategoryTitle(String categoryTitle) {
        // For FakeStore, we'll get all products and filter by category title
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getCategory() != null && 
                        product.getCategory().getTitle() != null && 
                        product.getCategory().getTitle().equalsIgnoreCase(categoryTitle))
                .toList();
    }

    @Override
    public List<Product> getProductsByCategoryId(Long categoryId) {
        // For FakeStore, we'll get all products and filter by category ID
        List<Product> allProducts = getAllProducts();
        return allProducts.stream()
                .filter(product -> product.getCategory() != null && 
                        product.getCategory().getId() != null && 
                        product.getCategory().getId().equals(categoryId))
                .toList();
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
