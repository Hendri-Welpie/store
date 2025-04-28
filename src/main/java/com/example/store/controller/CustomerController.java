package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;

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
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @GetMapping
    @Async
    @Cacheable(
            value = "customers",
            key = "#page != null && #size != null ? 'all_customer_' + #page + '_' + #size : 'all_customers'")
    public CompletableFuture<List<CustomerDTO>> getAllCustomers(
            @RequestParam(required = false) final String query,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (ObjectUtils.isNotEmpty(query)) {
                    return customerMapper.customersToCustomerDTOs(
                            customerRepository.findByNameContainingIgnoreCase(query));
                }
                return customerMapper.customersToCustomerDTOs(
                        customerRepository.findAll(getPageable(page, size)).getContent());
            } catch (Exception exception) {
                return Collections.emptyList();
            }
        });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "customers", key = "'all_customers'")
    public CustomerDTO createCustomer(@RequestBody Customer customer) {
        return customerMapper.customerToCustomerDTO(customerRepository.save(customer));
    }

    private Pageable getPageable(Integer page, Integer size) {
        if (ObjectUtils.isNotEmpty(page) && ObjectUtils.isNotEmpty(size)) {
            return PageRequest.of(page, size);
        }
        return Pageable.unpaged();
    }
}
