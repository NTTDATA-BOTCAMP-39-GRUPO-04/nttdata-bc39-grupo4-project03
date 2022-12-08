package com.nttdata.bc39.grupo04.api.composite;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditDTO;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.product.ProductDTO;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CompositeService {
    Mono<TransactionAtmDTO> makeDepositATM(String destinationAccountNumber, double amount);

    Mono<TransactionAtmDTO> makeWithdrawnATM(String destinationAccountNumber, double amount);

    Mono<TransactionAtmDTO> makeTransferAccount(TransactionTransferDTO body);

    Flux<MovementsReportDTO> getAllMovementsByAccount(String account);

    Flux<AccountDTO> getAccountAllByCustomer(String customerId);

    Mono<AccountDTO> getAccountByNumber(String accountNumber);

    Mono<AccountDTO> createAccount(AccountDTO dto);

    Flux<CustomerDto> getAllCustomers();

    Mono<CustomerDto> createCustomer(CustomerDto customerDto);

    //Reports
    Flux<AvailableAmountDailyDTO> getAvailableAmountDaily(String customerId);

    Flux<ComissionReportDTO> getAllComissionByProduct(String fechStart, String fechEnd);

    //Credit
    Flux<CreditDTO> getAllCreditByCustomer(String customerId);

    //Product
    Mono<ProductDTO> getProductByCode(String productId);
}
