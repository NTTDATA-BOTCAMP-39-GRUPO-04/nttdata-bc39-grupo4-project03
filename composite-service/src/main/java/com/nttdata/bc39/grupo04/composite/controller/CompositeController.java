package com.nttdata.bc39.grupo04.composite.controller;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardNumberDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardPaymentDTO;
import com.nttdata.bc39.grupo04.api.composite.*;
import com.nttdata.bc39.grupo04.api.credit.CreditDTO;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
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

    //ismael: Credit Endpoints
    @GetMapping(value = "/credit/customer/{customerId}")
    Flux<CreditDTO> getAllCreditByCustomer(@PathVariable("customerId") String customerId) {
        return service.getAllCreditByCustomer(customerId);
    }

    //product endpoints
    @GetMapping(value = "/product/findByCode/{code}")
    public Mono<ProductDTO> getProductByCode(@PathVariable(value = "code") String code) {
        return service.getProductByCode(code);
    }

}