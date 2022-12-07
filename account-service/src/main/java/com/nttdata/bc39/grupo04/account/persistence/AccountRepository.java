package com.nttdata.bc39.grupo04.account.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AccountRepository extends ReactiveCrudRepository<AccountEntity, ObjectId> {
    Mono<AccountEntity> findByAccount(String account);
    Mono<Void> deleteByAccount(String account);
}
