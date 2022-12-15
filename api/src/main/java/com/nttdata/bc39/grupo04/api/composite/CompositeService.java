package com.nttdata.bc39.grupo04.api.composite;

import org.springframework.web.bind.annotation.PathVariable;

import com.nttdata.bc39.grupo04.api.account.AccountDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardNumberDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardPaymentDTO;
import com.nttdata.bc39.grupo04.api.account.DebitCardReportDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditCardReportDTO;
import com.nttdata.bc39.grupo04.api.credit.CreditDTO;
import com.nttdata.bc39.grupo04.api.customer.ConsolidatedSummaryDTO;
import com.nttdata.bc39.grupo04.api.customer.CustomerDto;
import com.nttdata.bc39.grupo04.api.movements.MovementsReportDTO;
import com.nttdata.bc39.grupo04.api.product.GeneralReportDTO;
import com.nttdata.bc39.grupo04.api.product.ProductDTO;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CompositeService {
    Mono<TransactionAtmDTO> makeDepositATM(String destinationAccountNumber, double amount);

    Mono<TransactionAtmDTO> makeWithdrawnATM(String destinationAccountNumber, double amount);

    Mono<TransactionAtmDTO> makeTransferAccount(TransactionTransferDTO body);

    Flux<MovementsReportDTO> getAllMovementsByAccount(String account);

    Flux<AccountDTO> getAccountAllByCustomer(String customerId);

    Mono<DebitCardDTO> createDebitCard(DebitCardDTO debitCardDTO);

    Mono<DebitCardPaymentDTO> paymentWithDebitCard(DebitCardPaymentDTO debitCardPaymnetDTO);

    Mono<AccountDTO> getMainAccountByDebitCardNumber(String debitCardNumber);

    Mono<DebitCardNumberDTO> generateNumberDebitCard();

    Mono<AccountDTO> getAccountByNumber(String accountNumber);

    Mono<AccountDTO> createAccount(AccountDTO dto);

    Flux<CustomerDto> getAllCustomers();

    Mono<CustomerDto> createCustomer(CustomerDto customerDto);

    // Reports
    Flux<AvailableAmountDailyDTO> getAvailableAmountDaily(String customerId);

    Flux<ComissionReportDTO> getAllComissionByProduct(String fechStart, String fechEnd);
    
    Mono<DebitCardReportDTO> getLastTenDebitCardMovements(String debitCardNumber);
    
	Mono<CreditCardReportDTO> getLastTenCreditCardMovements(String debitCardNumber);
	
	Mono<ConsolidatedSummaryDTO> getConsolidatedSummary(String customerId);
	
    Mono<GeneralReportDTO> getReportGeneral(String fechStart, String fechEnd, String productId);

    // Credit

    Mono<CreditDTO> createCredit(CreditDTO dto);

    Mono<CreditDTO> getByCreditNumber(String creditNumber);

    Flux<CreditDTO> getAllCreditByCustomer(String customerId);

    Mono<CreditDTO> makePaymentCredit(double amount, String creditNumber, String payingCustomerId);

    Mono<CreditDTO> makePaymentCreditCard(double amount, String creditCardNumber);

    Mono<CreditDTO> makeChargeCredit(double amount, String creditCardNumber);

    Mono<Void> deleteCredit(String creditNumber);

    Flux<CreditDTO> getAllCreditCardByCustomer(String customerId);

    // Product
    Mono<ProductDTO> getProductByCode(String productId);

    Flux<ProductDTO> getAllProducts();

    Mono<ProductDTO> createProduct(ProductDTO dto);

    Mono<ProductDTO> updateProduct(ProductDTO dto);

    Mono<Void> deleteProductByCode(String code);
}
