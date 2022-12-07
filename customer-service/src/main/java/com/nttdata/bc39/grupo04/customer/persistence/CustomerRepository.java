package com.nttdata.bc39.grupo04.customer.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepository extends ReactiveCrudRepository<CustomerEntity, ObjectId> {

    Mono<CustomerEntity> findByCode(String code);

    Mono<Void> deleteByCode(String code);
}
