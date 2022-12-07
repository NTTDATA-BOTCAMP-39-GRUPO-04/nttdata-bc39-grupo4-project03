package com.nttdata.bc39.grupo04.credit;

import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEurekaClient
@ComponentScan({"com.nttdata.bc39.grupo04"})
public class CreditApplication {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        SpringApplication.run(CreditApplication.class, args);
    }

}
