package org.example.ecommserviceorder.orderservice.controller;

import jakarta.transaction.Transactional;
import org.example.ecommserviceorder.orderservice.models.*;
import org.example.ecommserviceorder.orderservice.repositories.IOrderItemRepository;
import org.example.ecommserviceorder.orderservice.repositories.IOrderRepository;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerBeanPostProcessorAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("api/v1/order")
public class OrderController {

    IOrderRepository orderRepository;
    IOrderItemRepository orderItemRepository;
    WebClient webClient;

    OrderController(IOrderRepository orderRepository, IOrderItemRepository orderItemRepository, WebClient webClient)
    {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.webClient = webClient;
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders()
    {
        List<Order> orders = orderRepository.findAll();
        return ResponseEntity.ok(orders);
    }

    @GetMapping({"orderId"})
    public ResponseEntity<Optional<Order>> getOrders(@PathVariable String orderId)
    {
        if(orderId == null || orderId.isEmpty())
        {
            return ResponseEntity.badRequest().build();
        }

        Optional<Order> order = orderRepository.findById(orderId);
        return ResponseEntity.ok(order);
    }

    @GetMapping("{customerId}")
    public ResponseEntity<List<Order>> getCustomerOrders(@PathVariable String customerId)
    {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("{orderId}/order-items")
    public ResponseEntity<List<OrderItem>> getOrderItems(@PathVariable String orderId)
    {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        return ResponseEntity.ok(orderItems);
    }

    @PostMapping("/place")
    @Transactional
    public  ResponseEntity<String> placeOrderForCustomer(@RequestBody Order order)
    {
        if(order == null || order.getCustomerId() == null || order.getCustomerId().isEmpty())
        {
            return ResponseEntity.badRequest().body("Customer id is required");
        }

        // Get Cart Items
        List<CartItem> cartItems = getCartItems(order.getCustomerId());
        // Check for inventory availability
        AtomicBoolean isSufficientInventory = new AtomicBoolean(true);
        List<InventoryItem> inventoryItems = new ArrayList<>();
        cartItems.forEach(x -> {
            InventoryItem item = getInventoryItem(x.getProductId());
            inventoryItems.add(item);
            if(item.getQuantity() <= x.getQuantity())
            {
                isSufficientInventory.set(false);
            }
        });

        if(!isSufficientInventory.get())
        {
            ResponseEntity.status(HttpStatus.CONFLICT).body("Insufficient items in inventory");
        }

        // Reduce Inventory Quantity
        cartItems.forEach(x -> {
            reduceInventoryItemQuantity(inventoryItems.stream().filter(y -> y.getProductId().equals(x.getProductId())).findFirst().get().getId(),x.getQuantity());
        });

        // Place Order
        String orderId = placeOrder(order);
        // Empty Cart Items
        makeCartEmpty(order.getCustomerId());
        // Return OrderId
        return ResponseEntity.ok("Order placed successfully with id " + orderId);
    }


    private InventoryItem getInventoryItem(String productId)
    {

        return webClient.get()
                .uri("ecomm-service-inventory/api/v1/inventory-item/product/"+productId)
                .retrieve().bodyToMono(InventoryItem.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatusCode.valueOf(404)) {
                        return Mono.empty(); // Return an empty Mono
                    } else {
                        return Mono.error(ex);
                    }
                })
                .block();
    }

    private List<CartItem> getCartItems(String customerId)
    {
        return webClient.get()
                .uri("ecomm-service-customer/api/v1/customers/"+customerId+"/cart")
                .retrieve().bodyToFlux(CartItem.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatusCode.valueOf(404)) {
                        return Mono.empty(); // Return an empty Mono
                    } else {
                        return Mono.error(ex);
                    }
                })
                .collectList()
                .block();
    }

    private void reduceInventoryItemQuantity(String inventoryItemId, int quantityToReduce)
    {
        webClient.post()
                .uri("ecomm-service-inventory/api/v1/inventory-item/"+inventoryItemId+"/remove-quantity/"+quantityToReduce)
                .retrieve().bodyToMono(Void.class)
                .block();
    }

    private void makeCartEmpty(String customerId)
    {
        webClient.delete()
                .uri("ecomm-service-customer/api/v1/customers/"+customerId+"/cart")
                .retrieve().bodyToMono(Void.class)
                .block();
    }

    private Product getProductDetails(String productId)
    {
        return webClient.get()
                .uri("ecomm-service-product/api/v1/products/"+productId)
                .retrieve().bodyToMono(Product.class)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatusCode.valueOf(404)) {
                        // Handle the "not found" case here
                        // For example, return a default MyModel or throw an exception
                        return Mono.empty(); // Return an empty Mono
                    } else {
                        // For other errors, rethrow the exception
                        return Mono.error(ex);
                    }
                })
                .block();
    }

    private String placeOrder(Order order)
    {
        //Get Cart Item
        List<CartItem> cartItems = getCartItems(order.getCustomerId());
        // Save Order
        order.setId(UUID.randomUUID().toString());
        order.setStatus("Placed");
        orderRepository.save(order);
        // Save OrderItem
        cartItems.forEach(x -> {
            OrderItem newItem = new OrderItem();
            Product product = getProductDetails(x.getProductId());
            newItem.setId(UUID.randomUUID().toString());
            newItem.setOrderId(order.getId());
            newItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(x.getQuantity())));
            newItem.setQuantity(x.getQuantity());
            newItem.setProductId(x.getProductId());
            orderItemRepository.save(newItem);
        });

        return order.getId();
    }

}
