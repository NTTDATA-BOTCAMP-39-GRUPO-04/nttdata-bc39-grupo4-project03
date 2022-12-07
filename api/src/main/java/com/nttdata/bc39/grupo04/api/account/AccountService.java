package com.nttdata.bc39.grupo04.api.account;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountService {

    Mono<AccountDTO> getByAccountNumber(String accountNumber);

    Flux<AccountDTO> getAllAccountByCustomer(String customerId);

    Mono<AccountDTO> createAccount(AccountDTO dto);

    Mono<AccountDTO> makeDepositAccount(double amount, String accountNumber);

    Mono<AccountDTO> makeWithdrawalAccount(double amount, String accountNumber);

    Mono<Void> deleteAccount(String accountNumber);

}
