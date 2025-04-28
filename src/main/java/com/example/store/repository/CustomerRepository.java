package com.example.store.repository;

import com.example.store.entity.Customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @EntityGraph(attributePaths = {"orders"})
    List<Customer> findByNameContainingIgnoreCase(String name);

    @Override
    @NonNull @EntityGraph(attributePaths = {"orders"})
    Page<Customer> findAll(@NonNull Pageable pageable);
}
