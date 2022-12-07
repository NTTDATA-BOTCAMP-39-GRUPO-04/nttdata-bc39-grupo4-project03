package com.nttdata.bc39.grupo04.credit.service;

import com.nttdata.bc39.grupo04.credit.dto.CreditDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CreditService {

	Mono<CreditDTO> createCredit(CreditDTO dto);

	Mono<CreditDTO> getByCreditNumber(String creditNumber);

	Flux<CreditDTO> getAllCreditByCustomer(String customerId);

	Mono<CreditDTO> makePaymentCredit(double amount, String creditNumber);

	Mono<CreditDTO> makePaymentCreditCard(double amount, String creditCardNumber);

	Mono<CreditDTO> makeChargeCredit(double amount, String cardNumber);

	Mono<Void> deleteCredit(String creditNumber);
	
	Flux<CreditDTO> getAllCreditCardByCustomer(String customerId);
}
