package com.nttdata.bc39.grupo04.customer.controller;

import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.customer.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/customer")
public class CustomerController {
    @Autowired
    private CustomerService service;

    @GetMapping(value = "/all")
    Flux<CustomerDto> getAllCustomers() {
        return service.getAllCustomers();
    }

    @GetMapping(value = "/{customerId}")
    Mono<CustomerDto> getCustomerById(@PathVariable(value = "customerId") String customerId) {
        return service.getCustomerById(customerId);
    }

    @PostMapping(value = "/save")
    Mono<CustomerDto> createCustomer(@RequestBody CustomerDto body) {
        return service.createCustomer(body);
    }

    @PutMapping(value = "/{customerId}")
    Mono<CustomerDto> updateCustomer(@PathVariable(value = "customerId") String customerId,
                                     @RequestBody CustomerDto body) {
        return service.updateCustomerById(customerId, body);
    }

    @DeleteMapping(value = "/{customerId}")
    Mono<Void> deleteCustomerByCode(@PathVariable(value = "customerId") String customerId) {
        return service.deleteCustomerById(customerId);
    }
}
