package com.example.userservice.handler;

import com.example.userservice.models.ErrorResponse;
import com.example.userservice.models.PayUserDetails;
import com.example.userservice.repository.UserServiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
public class UserServiceHandler {

    private final UserServiceRepository userServiceRepository;

    @Autowired
    public UserServiceHandler(UserServiceRepository userServiceRepository ) {
        this.userServiceRepository = userServiceRepository;
    }

    public Mono<ServerResponse> initUser( ServerRequest serverRequest ){
        Mono<String>  emailMono =  serverRequest.bodyToMono(String.class);

        return ServerResponse.ok().body( emailMono.flatMap(
                email -> userServiceRepository.save(new PayUserDetails(
                        email,
                        new ArrayList<>() ,
                        new ArrayList<>() ,
                        new ArrayList<>() )

                )) , PayUserDetails.class );
    }

    public Mono<ServerResponse> userDetails( ServerRequest serverRequest ){

        String email = serverRequest.pathVariable("email");

        return userServiceRepository.findByEmail(email).flatMap(
                payUserDetails -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                        .body( Mono.just(payUserDetails) , PayUserDetails.class )
                ).switchIfEmpty( ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                        Mono.just( new ErrorResponse("error no such user") ),
                ErrorResponse.class
        ) );

    }

    public Mono<ServerResponse> removeConnection( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");
        String toEmail = serverRequest.pathVariable("toEmail");

        Mono<PayUserDetails> toDetailsMono = userServiceRepository.findByEmail(toEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> connections = payUserDetails.getConnections();

                    connections.remove(fromEmail);

                    payUserDetails.setConnections(connections);

                    return Mono.just( payUserDetails);
                }
        ).flatMap(userServiceRepository::save);

        Mono<PayUserDetails>  fromDetailsMono = userServiceRepository.findByEmail(fromEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> connections = payUserDetails.getConnections();
                    connections.remove(toEmail);

                    payUserDetails.setConnections(connections);

                    return Mono.just( payUserDetails );
                }
        ).flatMap(userServiceRepository::save);

        return fromDetailsMono.zipWith( toDetailsMono , (to,from)-> from ).flatMap(
                payUserDetails -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body( Mono.just(payUserDetails) , PayUserDetails.class )
        ).switchIfEmpty(
                ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body( Mono.just( new ErrorResponse("error Due to invalid users -> "
                        + fromEmail + " or -> " + toEmail)) , ErrorResponse.class )
        );

    }

    public Mono<ServerResponse> acceptConnectionRequest( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");
        String toEmail = serverRequest.pathVariable("toEmail");

        Logger logger = LoggerFactory.getLogger(UserServiceHandler.class);

        Mono<PayUserDetails> toDetailsMono = userServiceRepository.findByEmail(toEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> connections = payUserDetails.getConnections();
                    ArrayList<String> connectionRequests = payUserDetails.getConnectionRequests();
                    ArrayList<String> pendingRequests = payUserDetails.getPendingRequests();

                    connections.add(fromEmail);
                    connectionRequests.remove(fromEmail);
                    pendingRequests.remove(fromEmail);

                    payUserDetails.setConnections(connections);
                    payUserDetails.setConnectionRequests(connectionRequests);
                    payUserDetails.setPendingRequests(pendingRequests);

                    logger.info(payUserDetails.toString());

                    return Mono.just( payUserDetails );
                }
        ).flatMap(userServiceRepository::save);

        Mono<PayUserDetails>  fromDetailsMono = userServiceRepository.findByEmail(fromEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> connections = payUserDetails.getConnections();
                    ArrayList<String> connectionRequests = payUserDetails.getConnectionRequests();
                    ArrayList<String> pendingRequests = payUserDetails.getPendingRequests();

                    connections.add(toEmail);
                    connectionRequests.remove(toEmail);
                    pendingRequests.remove(toEmail);

                    payUserDetails.setConnections(connections);
                    payUserDetails.setConnectionRequests(connectionRequests);
                    payUserDetails.setPendingRequests(pendingRequests);

                    logger.info(payUserDetails.toString());

                    return Mono.just(payUserDetails);
                }
        ).flatMap(userServiceRepository::save);

        return fromDetailsMono.zipWith( toDetailsMono , (to,from)-> from ).flatMap(
                payUserDetails -> ServerResponse.ok().body( Mono.just(payUserDetails) , PayUserDetails.class )
        ).switchIfEmpty( ServerResponse.ok().body( Mono.just( new ErrorResponse("error no such users") ) , ErrorResponse.class ) );

    }

    public Mono<ServerResponse> removeConnectionRequest( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");
        String toEmail = serverRequest.pathVariable("toEmail");

        Mono<PayUserDetails> toDetailsMono = userServiceRepository.findByEmail(toEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> connectionRequests = payUserDetails.getConnectionRequests();

                    connectionRequests.remove(fromEmail);

                    payUserDetails.setConnectionRequests(connectionRequests);

                    return Mono.just( payUserDetails );
                }
        ).flatMap(userServiceRepository::save)
                .switchIfEmpty( Mono.empty() );

        Mono<PayUserDetails>  fromDetailsMono = userServiceRepository.findByEmail(fromEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> pendingConnections = payUserDetails.getPendingRequests();

                    pendingConnections.remove(toEmail);
                    payUserDetails.setPendingRequests(pendingConnections);

                    return Mono.just( payUserDetails );
                }
        ).flatMap(userServiceRepository::save)
                .switchIfEmpty( Mono.empty() );

        return fromDetailsMono.zipWith( toDetailsMono , (to,from)-> from ).flatMap(
                payUserDetails -> ServerResponse.ok().body( Mono.just(payUserDetails) , PayUserDetails.class )
        ).switchIfEmpty( ServerResponse.ok().body( Mono.just( new ErrorResponse("error invalid users")) , ErrorResponse.class ) );

    }

    public Mono<ServerResponse> sendConnectionRequest( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");
        String toEmail = serverRequest.pathVariable("toEmail");

        Mono<PayUserDetails> toDetailsMono = userServiceRepository.findByEmail(toEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> connectionRequests = payUserDetails.getConnectionRequests();

                    if(
                            connectionRequests.contains(fromEmail) ||
                                    payUserDetails.getConnections().contains(fromEmail)
                    )
                        return Mono.empty();

                    connectionRequests.add(fromEmail);

                    payUserDetails.setConnectionRequests(connectionRequests);

                    return Mono.just(payUserDetails);

                }
        ).flatMap(userServiceRepository::save);

        Mono<PayUserDetails>  fromDetailsMono = userServiceRepository.findByEmail(fromEmail).flatMap(
                payUserDetails -> {

                    ArrayList<String> pendingRequests = payUserDetails.getPendingRequests();

                    if( pendingRequests.contains(toEmail) ||
                            payUserDetails.getConnections().contains(toEmail)
                    )
                        return Mono.empty();

                    pendingRequests.add(toEmail);

                    payUserDetails.setPendingRequests(pendingRequests);

                    return Mono.just(payUserDetails);

                }

        ).flatMap(userServiceRepository::save);

        return toDetailsMono.zipWith( fromDetailsMono , (from,to)-> from ).flatMap(
                payUserDetails -> ServerResponse.ok().body( Mono.just(payUserDetails) , PayUserDetails.class )
        ).switchIfEmpty(
                ServerResponse.ok().body(
                        Mono.just(new ErrorResponse("error connection already exists or request already sent")),
                        ErrorResponse.class)
        );

    }

}
