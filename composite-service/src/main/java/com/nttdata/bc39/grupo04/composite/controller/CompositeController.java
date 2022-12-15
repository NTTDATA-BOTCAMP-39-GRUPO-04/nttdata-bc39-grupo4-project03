package com.nttdata.bc39.grupo04.composite.controller;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardNumberDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardPaymentDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardReportDTO;
import com.nttdata.bc39.grupo04.api.composite.*;
import com.nttdata.bc39.grupo04.api.credit.CreditCardReportDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditDTO;
import com.nttdata.bc39.grupo04.api.customer.ConsolidatedSummaryDTO;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.product.GeneralReportDTO;
import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/composite")
public class CompositeController {

    @Autowired
    private CompositeService service;

    @PutMapping("/depositAtm/{account}")
    Mono<TransactionAtmDTO> makeDepositeATM(@PathVariable("account") String account, @RequestParam("amount") double amount) {
        return service.makeDepositATM(account, amount);
    }

    @PutMapping("/withdrawalAtm/{account}")
    Mono<TransactionAtmDTO> makeWithdrawlATM(@PathVariable("account") String account, @RequestParam("amount") double amount) {
        return service.makeWithdrawnATM(account, amount);
    }

    @PutMapping("/tranference")
    Mono<TransactionAtmDTO> makeTransference(@RequestBody TransactionTransferDTO dto) {
        return service.makeTransferAccount(dto);
    }

    @GetMapping("/movements/{account}")
    Flux<MovementsReportDTO> getAllMovementsByNumber(@PathVariable("account") String account) {
        return service.getAllMovementsByAccount(account);
    }

    @GetMapping("account/customer/{customerId}")
    Flux<AccountDTO> getAccountAllByCustomer(@PathVariable(value = "customerId") String customerId) {
        return service.getAccountAllByCustomer(customerId);
    }

    @GetMapping(value = "/account/{accountNumber}")
    Mono<AccountDTO> getAccountByNumber(@PathVariable(value = "accountNumber") String accountNumber) {
        return service.getAccountByNumber(accountNumber);
    }

    @PostMapping("/account/debitCard/associatedWithAccounts")
    Mono<DebitCardDTO> createDebitCard(@RequestBody DebitCardDTO debitCardDTO) {
        return service.createDebitCard(debitCardDTO);
    }

    @GetMapping("/account/debitCard/generateNumber")
    Mono<DebitCardNumberDTO> generateNumberDebitCard() {
        return service.generateNumberDebitCard();
    }

    @PostMapping("/account/debitCard/payment")
    Mono<DebitCardPaymentDTO> paymentDebitCard(@RequestBody DebitCardPaymentDTO body) {
        return service.paymentWithDebitCard(body);
    }

    @GetMapping("/account/debitCard/main/{debitCardNumber}")
    Mono<AccountDTO> getMainAccountByDebitCardNumber(@PathVariable("debitCardNumber") String debitCardNumber){
        return service.getMainAccountByDebitCardNumber(debitCardNumber);
    }

    @PostMapping("/account/save")
    Mono<AccountDTO> createAccount(@RequestBody AccountDTO dto) {
        return service.createAccount(dto);
    }
    
    @GetMapping("/report/lastTenDebitCard/{debitCardNumber}")
    Mono<DebitCardReportDTO> getLastTenDebitCardMovements(@PathVariable("debitCardNumber") String debitCardNumber){
        return service.getLastTenDebitCardMovements(debitCardNumber);
    }
    
    @GetMapping("/report/lastTenCreditCard/{creditCardNumber}")
    Mono<CreditCardReportDTO> getLastTenCreditCardMovements(@PathVariable("creditCardNumber") String creditCardNumber){
        return service.getLastTenCreditCardMovements(creditCardNumber);
    }
    
    @GetMapping("/report/consolidatedSummary/{customerId}")
    Mono<ConsolidatedSummaryDTO> getConsolidatedSummary(@PathVariable("customerId") String customerId){
        return service.getConsolidatedSummary(customerId);
    }
    
    @GetMapping("/report/general/{productId}")
    Mono<GeneralReportDTO> getReportGeneral(@PathVariable("productId") String productId,
    		@RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate){
        return service.getReportGeneral(startDate,endDate,productId);
    }

    @GetMapping("/customer/all")
    Flux<CustomerDto> getAllCustomer() {
        return service.getAllCustomers();
    }

    @PostMapping("/customer/save")
    Mono<CustomerDto> createCustomer(@RequestBody CustomerDto customerDto) {
        return service.createCustomer(customerDto);
    }

    @GetMapping("/availableAmountDailyAVG/{customerId}")
    Flux<AvailableAmountDailyDTO> getAvailableAmountDailyAVG(@PathVariable("customerId") String customerId) {
        return service.getAvailableAmountDaily(customerId);
    }

    @GetMapping("/report/totalComissionByProduct")
    Flux<ComissionReportDTO> getTotalComissionByProduct(@RequestParam("fechStart") String fechStart,
                                                        @RequestParam("fechEnd") String fechEnd) {
        return service.getAllComissionByProduct(fechStart, fechEnd);
    }
       
    @GetMapping(value = "/credit/customer/{customerId}")
    Flux<CreditDTO> getAllCreditByCustomer(@PathVariable("customerId") String customerId) {
        return service.getAllCreditByCustomer(customerId);
    }
    
	@GetMapping(value = "/credit/{creditNumber}")
	Mono<CreditDTO> getByCreditNumber(@PathVariable(value = "creditNumber") String creditNumber) {
		return service.getByCreditNumber(creditNumber);
	}
    
	@PostMapping("/credit/save")
	Mono<CreditDTO> createAccount(@RequestBody CreditDTO dto) {
		return service.createCredit(dto);
	}
    
	@PutMapping("/credit/paymentcredit/{creditNumber}")
	Mono<CreditDTO> makePaymentCredit(@PathVariable(value = "creditNumber") String creditNumber,
			@RequestParam(value = "amount") double amount) {
		return service.makePaymentCredit(amount, creditNumber,"");
	}
	
	@PutMapping("/credit/thirdpartycreditpayment/{creditNumber}/{customerId}")
	Mono<CreditDTO> thirdPartyCreditPayment(@PathVariable(value = "creditNumber") String creditNumber,
			@PathVariable(value = "customerId") String customerId,
			@RequestParam(value = "amount") double amount) {
		return service.makePaymentCredit(amount, creditNumber,customerId);
	}
	
	@PutMapping("/credit/paymentcreditcard/{creditCardNumber}")
	Mono<CreditDTO> makePaymentCreditCard(@PathVariable(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "amount") double amount) {
		return service.makePaymentCreditCard(amount, creditCardNumber);
	}
	
	@PutMapping("/credit/chargecreditcard/{creditCardNumber}")
	Mono<CreditDTO> makeChargeCredit(@PathVariable(value = "creditCardNumber") String creditCardNumber,
			@RequestParam(value = "amount") double amount) {
		return service.makeChargeCredit(amount, creditCardNumber);
	}
	
	@DeleteMapping("/credit/{creditNumber}")
	Mono<Void> deleteCredit(@PathVariable(value = "creditNumber") String creditNumber) {
		return service.deleteCredit(creditNumber);
	}

	@GetMapping(value = "/credit/getcreditcardcustomer/{customerId}")
	Flux<CreditDTO> getAllCreditCardByCustomer(@PathVariable(value = "customerId") String customerId) {
		return service.getAllCreditCardByCustomer(customerId);
	}
	

    //product endpoints
    @GetMapping(value = "/product/findByCode/{code}")
    public Mono<ProductDTO> getProductByCode(@PathVariable(value = "code") String code) {
        return service.getProductByCode(code);
    }
    
    @GetMapping(value = "/product/findAll")
    public Flux<ProductDTO> getAllProducts() {
        return service.getAllProducts();
    }
    
    @PostMapping(value = "/product/save")
    public Mono<ProductDTO> createProduct(@RequestBody ProductDTO body) {
        return service.createProduct(body);
    }

    @PutMapping(value = "/product/update")
    public Mono<ProductDTO> updateProduct(@RequestBody ProductDTO  body) {
        return service.updateProduct(body);
    }

     @DeleteMapping(value = "/product/delete/{code}")
    public Mono<Void> deleteProduct(@PathVariable(value = "code") String code) {
        return service.deleteProductByCode(code);
    }

}