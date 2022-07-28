package com.example.authservice.handler;

import com.example.authservice.models.*;
import com.example.authservice.repo.AuthServiceRepository;
import com.example.authservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class AuthServiceHandler {

    private final JwtUtil jwtUtil;

    private final AuthServiceRepository authServiceRepository;

    private final WebClient userServiceWebClient;

    private final WebClient transactionServiceWebClient;

    @Autowired
    public AuthServiceHandler( JwtUtil jwtUtil ,
                               AuthServiceRepository authServiceRepository ) {
        this.jwtUtil = jwtUtil;
        this.authServiceRepository = authServiceRepository;
        this.userServiceWebClient = WebClient.builder().baseUrl("http://localhost:9003/user/").build();
        this.transactionServiceWebClient = WebClient.builder().baseUrl("http://localhost:9004/transaction/user").build();
    }

    public Mono<ServerResponse> getUser( ServerRequest serverRequest ){

        String email = serverRequest.pathVariable("email");

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                authServiceRepository.findByEmail(email) ,
                PayUser.class
        );

    }

    public Mono<ServerResponse> registerUser(ServerRequest serverRequest ) {

        String email = serverRequest.pathVariable("email");

        Mono<PayUser> storedPayUserMono = authServiceRepository.findByEmail(email).switchIfEmpty( Mono.empty() );

        Mono<PayUser> payUserMono = serverRequest.bodyToMono(PayUser.class);

        Mono<PayUser> payUserResultMono = storedPayUserMono.hasElement().zipWith(payUserMono, (aBoolean, payUser) -> aBoolean ? new PayUser() : payUser)
                .flatMap(
                        payUser -> {
                            if (payUser.getEmail() == null) {
                                return Mono.empty();
                            } else {
                                return authServiceRepository.save(payUser);
                            }
                        }
                );

       return payUserResultMono.flatMap(
               payUser ->
                      Mono.zip(
                              userServiceWebClient.post().bodyValue(payUser.getEmail()).retrieve().bodyToMono(PayUserDetails.class).log(),
                              transactionServiceWebClient.post().bodyValue(payUser).retrieve().bodyToMono(PayUserDetails.class).log()
               ).flatMap(
                       data -> ServerResponse.ok().body( Mono.just( data.getT1() )
                        ,PayUserDetails.class)
        )
       ).switchIfEmpty(ServerResponse.ok().body(Mono.just(new ErrorResponse("User Already Exists")), ErrorResponse.class));
    }

    public Mono<ServerResponse> loginUser( ServerRequest serverRequest ){

        String email = serverRequest.pathVariable("email");

        Mono<PayUser> payUserMono = authServiceRepository.findByEmail(email).switchIfEmpty( Mono.empty() );

        Mono<AuthRequest> authRequestMono = serverRequest.bodyToMono(AuthRequest.class);

        return Mono.zip( authRequestMono , payUserMono ).flatMap(
                user -> {

                    if( user.getT1().getPassword().equals(user.getT2().getPassword()) &&
                    user.getT1().getEmail().equals(user.getT2().getEmail()) ){

                        String accessToken = jwtUtil.generate( user.getT2());

                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .body( Mono.just(  new AuthResponse(accessToken) ) , AuthResponse.class );

                    }
                    else {
                        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .body(Mono.just( new ErrorResponse("Invalid Credentials") ), ErrorResponse.class);
                    }
                }
        ).switchIfEmpty( ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body( Mono.just(new ErrorResponse("No Such User Found")) , ErrorResponse.class ) );

    }

}
