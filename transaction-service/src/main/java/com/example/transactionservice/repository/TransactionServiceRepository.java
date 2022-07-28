package com.example.transactionservice.repository;

import com.example.transactionservice.models.TransactionDetails;
import com.example.transactionservice.models.TransactionId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public interface TransactionServiceRepository extends ReactiveMongoRepository<TransactionDetails, String> {

    Flux<TransactionDetails> findTransactionDetailsByTransactionId(TransactionId transactionId);

    Mono<TransactionDetails> findByUniqueId( String uniqueId );

    Mono<TransactionDetails> deleteByUniqueId(String s);

}
