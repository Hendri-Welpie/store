package com.example.store.controller;

import com.example.store.dto.CustomerDTO;
import com.example.store.entity.Customer;
import com.example.store.mapper.CustomerMapper;
import com.example.store.repository.CustomerRepository;
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

@WebMvcTest(CustomerController.class)
@ComponentScan(basePackageClasses = CustomerMapper.class)
class CustomerControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerRepository customerRepository;

    @MockitoBean
    private CustomerMapper customerMapper;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);
    }

    @Test
    void testCreateCustomer() throws Exception {
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(customerMapper.customerToCustomerDTO(any(Customer.class)))
                .thenReturn(new CustomerDTO(1L, "John Doe", null));

        mockMvc.perform(post("/customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(customer)))
                .andExpect(status().isCreated())
                .andExpect(content().json(toJson(new CustomerDTO(1L, "John Doe", null))));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(customerRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(customer)));

        when(customerMapper.customersToCustomerDTOs(anyList()))
                .thenReturn(List.of(new CustomerDTO(1L, "John Doe", null)));

        mockMvc.perform(asyncDispatch(mockMvc.perform(get("/customer"))
                        .andExpect(request().asyncStarted())
                        .andReturn()))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .json(
                                        """
                            [
                              {"id":1,"name":"John Doe","orders":null}
                            ]
                        """));
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
