package com.example.store.dto;

import com.example.store.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Long id;
    private String description;
    private OrderCustomerDTO customer;
    private List<Product> products;
}
