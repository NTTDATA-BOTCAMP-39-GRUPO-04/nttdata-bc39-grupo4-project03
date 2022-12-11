package com.nttdata.bc39.grupo04.account.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, ObjectId> {
    Mono<AccountEntity> findByAccount(String account);

    Flux<AccountEntity> findByDebitCardNumber(String debitCardNumber);

    Mono<Void> deleteByAccount(String account);
}
