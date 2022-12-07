package com.nttdata.bc39.grupo04.credit.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;

@Repository
public interface CreditRepository extends ReactiveCrudRepository<CreditEntity, ObjectId> {
	Mono<CreditEntity> findByCreditNumber(String creditNumber);

	Mono<Void> deleteByCreditNumber(String creditNumber);

	Mono<CreditEntity> findByCardNumber(String cardNumber);
}
