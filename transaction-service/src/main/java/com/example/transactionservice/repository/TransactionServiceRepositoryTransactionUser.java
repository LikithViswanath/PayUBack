package com.example.transactionservice.repository;

import com.example.transactionservice.models.TransactionUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface TransactionServiceRepositoryTransactionUser extends ReactiveMongoRepository<TransactionUser,String> {

    Mono<TransactionUser> findByEmail(String email);

}
