package org.example.ecommserviceorder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.example.ecommserviceorder.orderservice.models.Order;
import org.example.ecommserviceorder.orderservice.models.Payment;
import org.example.ecommserviceorder.orderservice.repositories.IOrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.time.Instant;
import java.util.Optional;

@Component
public class KafkaConsumer {

    IOrderRepository orderRepository;

    KafkaConsumer(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = "${kafka.topic.name}", groupId = "${kafka.consumer.name}")
    public void listen(String message) {
        System.out.println(message);
        String orderId = message.substring(1, message.length() - 1);
        Order order =  processOrder(orderId);
        // from here we can start logistics and close the order.
        completeOrder(order);
    }

    public Order processOrder(String orderId)
    {

        Optional<Order> order = orderRepository.findById(orderId);
        order.get().setStatus("Processing");
        orderRepository.save(order.get());
        return  order.get();
    }

    public void completeOrder(Order order)
    {
        order.setStatus("Complete");
        orderRepository.save(order);
    }
}

