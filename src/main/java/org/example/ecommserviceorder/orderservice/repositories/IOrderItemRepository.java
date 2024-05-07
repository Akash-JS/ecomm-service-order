package org.example.ecommserviceorder.orderservice.repositories;

import org.example.ecommserviceorder.orderservice.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IOrderItemRepository extends JpaRepository<OrderItem,String> {
    List<OrderItem> findByOrderId(String orderId);
}
