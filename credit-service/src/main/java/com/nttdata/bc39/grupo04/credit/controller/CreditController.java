package com.nttdata.bc39.grupo04.credit.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nttdata.bc39.grupo04.credit.dto.CreditDTO;
import com.nttdata.bc39.grupo04.credit.service.CreditService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/credit")
public class CreditController {
	@Autowired
	private CreditService service;

	@GetMapping(value = "/customer/{customerId}")
	Flux<CreditDTO> getAllCreditByCustomer(@PathVariable(value = "customerId") String customerId) {
		return service.getAllCreditByCustomer(customerId);
	}

	@GetMapping(value = "/{creditNumber}")
	Mono<CreditDTO> getByCreditNumber(@PathVariable(value = "creditNumber") String creditNumber) {
		return service.getByCreditNumber(creditNumber);
	}

	@PostMapping("/save")
	Mono<CreditDTO> createAccount(@RequestBody CreditDTO dto) {
		CreditDTO.of(dto);
		return service.createCredit(dto);
	}

	@PutMapping("/paymentcredit/{creditNumber}")
	Mono<CreditDTO> makePaymentCredit(@PathVariable(value = "creditNumber") String creditNumber,
			@RequestParam(value = "amount") double amount) {
		return service.makePaymentCredit(amount, creditNumber);
	}

	@PutMapping("/paymentcreditcard/{creditCardNumber}")
	Mono<CreditDTO> makePaymentCreditCard(@PathVariable(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "amount") double amount) {
		return service.makePaymentCreditCard(amount, creditCardNumber);
	}

	@PutMapping("/chargecreditcard/{creditCardNumber}")
	Mono<CreditDTO> makeChargeCredit(@PathVariable(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "amount") double amount) {
		return service.makeChargeCredit(amount, creditCardNumber);
	}

	@DeleteMapping("/{creditNumber}")
	Mono<Void> deleteCredit(@PathVariable(value = "creditNumber") String creditNumber) {
		return service.deleteCredit(creditNumber);
	}
	
	@GetMapping(value = "/getcreditcardcustomer/{customerId}")
	Flux<CreditDTO> getAllCreditCardByCustomer(@PathVariable(value = "customerId") String customerId) {
		return service.getAllCreditCardByCustomer(customerId);
	}
}
