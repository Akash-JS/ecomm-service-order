package org.example.ecommserviceorder.orderservice.repositories;

import org.example.ecommserviceorder.orderservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderRepository extends JpaRepository<Order,String> {
    List<Order> findByCustomerId(String customerId);
}
