package com.example.authservice.repo;

import com.example.authservice.models.PayUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface AuthServiceRepository extends ReactiveMongoRepository<PayUser, ObjectId> {
    Mono<PayUser> findByEmail(String email);
}
