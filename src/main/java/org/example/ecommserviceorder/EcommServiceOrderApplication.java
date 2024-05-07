package org.example.ecommserviceorder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EcommServiceOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommServiceOrderApplication.class, args);
    }

}
