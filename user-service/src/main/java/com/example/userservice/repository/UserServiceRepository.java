package com.example.userservice.repository;

import com.example.userservice.models.PayUserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface UserServiceRepository extends ReactiveMongoRepository<PayUserDetails,String> {
    Mono<PayUserDetails> findByEmail( String email );
}
