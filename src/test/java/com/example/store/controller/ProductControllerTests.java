package com.example.store.controller;

import com.example.store.dto.ProductDTO;
import com.example.store.entity.Product;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(ProductController.class)
@ComponentScan(basePackageClasses = ProductMapper.class)
public class ProductControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductMapper productMapper;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setDescription("Vortex Viper Bino 12x50");
        product.setId(1L);
    }

    @Test
    void testCreateProducts() throws Exception {
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.productToProductDTO(any(Product.class)))
                .thenReturn(new ProductDTO(1L, "Vortex Viper Bino 12x50", List.of()));

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(product)))
                .andExpect(status().isCreated())
                .andExpect(content().json(toJson(new ProductDTO(1L, "Vortex Viper Bino 12x50", List.of()))));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(product)));

        when(productMapper.productsToProductDTOs(anyList()))
                .thenReturn(List.of(new ProductDTO(1L, "Vortex Viper Bino 12x50", null)));

        mockMvc.perform(asyncDispatch(mockMvc.perform(get("/products"))
                        .andExpect(request().asyncStarted())
                        .andReturn()))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .json(
                                        """
                            [
                              {"id":1,"description":"Vortex Viper Bino 12x50","orderIds":null}
                            ]
                        """));
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
