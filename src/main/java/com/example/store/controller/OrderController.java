package com.example.store.controller;

import com.example.store.dto.OrderDTO;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;

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
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    @GetMapping
    @Async
    @Cacheable(
            value = "orders",
            key =
                    "#id != null ? 'order_' + #id : (#page != null && #size != null ? 'all_order_' + #page + '_' + #size : 'all_orders')")
    public CompletableFuture<List<OrderDTO>> getAllOrders(
            @RequestParam(required = false) final Long id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (ObjectUtils.isNotEmpty(id)) {
                    return Collections.singletonList(orderMapper.orderToOrderDTO(
                            orderRepository.findById(id).orElse(null)));
                }
                return orderMapper.ordersToOrderDTOs(
                        orderRepository.findAll(getPageable(page, size)).getContent());
            } catch (Exception exception) {
                return Collections.emptyList();
            }
        });
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "orders", key = "'all_orders'")
    public OrderDTO createOrder(@RequestBody Order order) {
        Customer customer = customerRepository
                .findById(order.getCustomer().getId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        order.setCustomer(customer);
        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }

    private Pageable getPageable(Integer page, Integer size) {
        if (ObjectUtils.isNotEmpty(page) && ObjectUtils.isNotEmpty(size)) {
            return PageRequest.of(page, size);
        }
        return Pageable.unpaged();
    }
}
