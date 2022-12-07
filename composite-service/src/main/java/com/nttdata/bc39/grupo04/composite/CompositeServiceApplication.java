package com.nttdata.bc39.grupo04.composite;


import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan({"com.nttdata.bc39.grupo04"})
@EnableEurekaClient
public class CompositeServiceApplication {
    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(CompositeServiceApplication.class, args);
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

}