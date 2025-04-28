package com.example.store.controller;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "products", key = "'all_products'")
    public ProductDTO saveProduct(@RequestBody Product product) {
        return productMapper.productToProductDTO(productRepository.save(product));
    }

    @GetMapping
    @Async
    @Cacheable(
            value = "products",
            key =
                    "#id != null ? 'product_' + #id : (#page != null && #size != null ? 'all_product_' + #page + '_' + #size : 'all_products')")
    public CompletableFuture<List<ProductDTO>> getProducts(
            @RequestParam(required = false) final Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (ObjectUtils.isNotEmpty(id)) {
                    return productMapper.productsToProductDTOs(Collections.singletonList(
                            productRepository.findById(id).orElse(null)));
                }

                return productMapper.productsToProductDTOs(
                        productRepository.findAll(getPageable(page, size)).getContent());
            } catch (Exception exception) {
                return Collections.emptyList();
            }
        });
    }

    private Pageable getPageable(Integer page, Integer size) {
        if (ObjectUtils.isNotEmpty(page) && ObjectUtils.isNotEmpty(size)) {
            return PageRequest.of(page, size);
        }
        return Pageable.unpaged();
    }
}
