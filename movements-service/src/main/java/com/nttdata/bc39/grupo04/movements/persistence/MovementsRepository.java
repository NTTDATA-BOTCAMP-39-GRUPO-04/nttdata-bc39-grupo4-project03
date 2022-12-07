package com.nttdata.bc39.grupo04.movements.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MovementsRepository extends ReactiveCrudRepository<MovementsEntity, ObjectId> {
    Flux<MovementsEntity> findByAccount(String accountNumber);
}
