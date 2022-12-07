package com.nttdata.bc39.grupo04.product;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEurekaClient
@ComponentScan({"com.nttdata.bc39.grupo04"})
public class ProductServiceApplication {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(ProductServiceApplication.class, args);
    }
}