package com.example.store.controller;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@WebMvcTest(OrderController.class)
@RequiredArgsConstructor
public class OrdersControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderMapper orderMapper;

    @MockitoBean
    private CustomerRepository customerRepository;

    private Order order;
    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("John Doe");
        customer.setId(1L);

        order = new Order();
        order.setDescription("Test Order");
        order.setId(1L);
        order.setCustomer(customer);
    }

    @Test
    void testCreateProducts() throws Exception {
        when(customerRepository.findById(any(Long.class))).thenReturn(Optional.of(new Customer(1L, "John Doe", null)));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.orderToOrderDTO(any(Order.class)))
                .thenReturn(new OrderDTO(1L, "Test Order", new OrderCustomerDTO(1L, "John Doe"), List.of()));

        mockMvc.perform(post("/order").contentType(MediaType.APPLICATION_JSON).content(toJson(order)))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .json(toJson(new OrderDTO(1L, "Test Order", new OrderCustomerDTO(1L, "John Doe"), List.of()))));
    }

    @Test
    void testGetAllCustomers() throws Exception {
        when(orderRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(order)));

        when(orderMapper.ordersToOrderDTOs(anyList()))
                .thenReturn(List.of(new OrderDTO(1L, "Test Order", new OrderCustomerDTO(1L, "John Doe"), List.of())));

        mockMvc.perform(asyncDispatch(mockMvc.perform(get("/order"))
                        .andExpect(request().asyncStarted())
                        .andReturn()))
                .andExpect(status().isOk())
                .andExpect(
                        content()
                                .json(
                                        """
                        [{"id":1,"description":"Test Order","customer":{"id":1,"name":"John Doe"},"products":[]}]
                                                """));
    }

    private String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }
}
