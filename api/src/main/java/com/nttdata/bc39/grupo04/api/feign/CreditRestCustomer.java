package com.nttdata.bc39.grupo04.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.nttdata.bc39.grupo04.api.credit.CreditCustomerDTO;

import reactor.core.publisher.Flux;

@FeignClient(name = "credit-service")
public interface CreditRestCustomer {
	
	@GetMapping(path = "/getcreditcardcustomer/{customerId}")
	public Flux<CreditCustomerDTO> listCreditCardCustomer(@PathVariable(value = "customerId") String customerId);

}