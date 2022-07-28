package com.example.transactionservice.handler;

import com.example.transactionservice.models.*;
import com.example.transactionservice.repository.TransactionServiceRepository;
import com.example.transactionservice.repository.TransactionServiceRepositoryTransactionUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
public class TransactionServiceHandler {

    private final TransactionServiceRepository transactionServiceRepository;

    private final TransactionServiceRepositoryTransactionUser transactionServiceRepositoryTransactionUser;

    private final WebClient webClient;

    @Autowired
    public TransactionServiceHandler(TransactionServiceRepository transactionServiceRepository,
                                     TransactionServiceRepositoryTransactionUser transactionServiceRepositoryTransactionUser ) {
        this.transactionServiceRepository = transactionServiceRepository;
        this.transactionServiceRepositoryTransactionUser = transactionServiceRepositoryTransactionUser;
        this.webClient = WebClient.builder().baseUrl("http://schedule-service:9005//run-job").build();
    }

    public Mono<ServerResponse> test( ServerRequest serverRequest ){
        return ServerResponse.ok().body( Mono.just("test Successful") , String.class );
    }

    public Mono<ServerResponse> initTransactionUser( ServerRequest serverRequest ){

        Mono<PayUser> payUserMono = serverRequest.bodyToMono(PayUser.class);

        return payUserMono.flatMap(
              payUser -> ServerResponse.ok().body(  transactionServiceRepositoryTransactionUser.save(
                        new TransactionUser(
                                payUser.getEmail(),
                                payUser.getFirstName(),
                                payUser.getLastName(),
                                new ArrayList<>(),
                                new ArrayList<>(),
                                new ArrayList<>()
                        )
                ),
                      TransactionUser.class
              )
        );

    }

    public Mono<ServerResponse> getTransaction(ServerRequest serverRequest){

        String uniqueId = serverRequest.pathVariable("uniqueId");

        return transactionServiceRepository.findByUniqueId(uniqueId).flatMap(
                transactionDetails -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body( Mono.just(transactionDetails) , TransactionRequest.class ).log()
        ).switchIfEmpty( ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body( Mono.just( new ErrorResponse("error no such Transaction exists") ) , ErrorResponse.class )
        ).log();

    }

    public Mono<ServerResponse> getUserTransactions(ServerRequest serverRequest){

        String email = serverRequest.pathVariable("email");

        return transactionServiceRepositoryTransactionUser.findByEmail(email)
                .flatMap(
                        transactionUser -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .body( Mono.just(transactionUser) , TransactionUser.class )
                ).switchIfEmpty(
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                                .body( Mono.just(new ErrorResponse("error no such user")) , ErrorResponse.class )
                );

    }

    public Mono<ServerResponse> getTransactions(ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");
        String toEmail = serverRequest.pathVariable("toEmail");

        Flux<TransactionDetails> transactionDetailsFluxBorrow =  transactionServiceRepository.findTransactionDetailsByTransactionId( new TransactionId(toEmail,fromEmail) )
                .switchIfEmpty( Mono.empty() );

        Flux<TransactionDetails> transactionDetailsFluxLend =  transactionServiceRepository.findTransactionDetailsByTransactionId( new TransactionId(fromEmail,toEmail) )
                .switchIfEmpty( Mono.empty() );

        return transactionDetailsFluxBorrow.collectList()
                .zipWith( transactionDetailsFluxLend.collectList() ,  (borrow,lend) ->{
                    lend.addAll(borrow);
                    return  lend;
                } )
                .flatMap(
                        transactionDetailsList -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(
                                Mono.just(transactionDetailsList),
                                TransactionDetails.class
                        )
                ).switchIfEmpty(
                        ServerResponse.ok().body( Mono.just( new ErrorResponse("error no such users") ) , ErrorResponse.class )
                );

    }

    public Mono<ServerResponse> sendBorrowRequest( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");

        String toEmail = serverRequest.pathVariable("toEmail");

        Mono<TransactionDetails> transactionDetailsMono = serverRequest.bodyToMono(TransactionDetails.class);

        Mono<TransactionUser> toTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(toEmail);

        Mono<TransactionUser> fromTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(fromEmail);

        return Mono.zip( transactionDetailsMono , toTransactionUserMono , fromTransactionUserMono )
                .flatMap(
                        data ->{

                            data.getT1().setUniqueId(  String.valueOf(System.currentTimeMillis())  );

                            data.getT1().setTransactionId( new TransactionId( toEmail , fromEmail ) );

                            ArrayList<TransactionRequest> toBorrowRequests = data.getT2().getBorrowRequests();

                            ArrayList<TransactionRequest> fromPendingBorrowRequests = data.getT3().getPendingBorrowRequests();

                            toBorrowRequests.add( new TransactionRequest(
                                    data.getT1().getUniqueId(),
                                    data.getT3().getFirstName(),
                                    data.getT3().getEmail(),
                                    data.getT1().getAmount(),
                                    data.getT1().getDays(),
                                    data.getT1().getInterest()
                            ) );

                            fromPendingBorrowRequests.add( new TransactionRequest(
                                    data.getT1().getUniqueId(),
                                    data.getT2().getFirstName(),
                                    data.getT2().getEmail(),
                                    data.getT1().getAmount(),
                                    data.getT1().getDays(),
                                    data.getT1().getInterest()
                            ) );

                            data.getT2().setBorrowRequests(toBorrowRequests);
                            data.getT3().setPendingBorrowRequests(fromPendingBorrowRequests);

                            return Mono.zip(
                                    transactionServiceRepository.save(data.getT1()),
                                    transactionServiceRepositoryTransactionUser.save(data.getT2()),
                                    transactionServiceRepositoryTransactionUser.save(data.getT3())
                            ).flatMap(
                                    updatedData -> ServerResponse
                                            .ok().contentType(MediaType.APPLICATION_JSON)
                                            .body( Mono.just(updatedData.getT1()) , TransactionDetails.class )
                            );

                        }
                ).switchIfEmpty(
                        ServerResponse.ok().body( Mono.just( new ErrorResponse("error no such users") ) , ErrorResponse.class )
                );

    }

    public Mono<ServerResponse> acceptBorrowRequests( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");

        String toEmail = serverRequest.pathVariable("toEmail");

        String uniqueId = serverRequest.pathVariable("uniqueId");

        Mono<TransactionUser> toTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(toEmail);

        Mono<TransactionUser> fromTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(fromEmail);

        Mono<TransactionDetails> transactionDetailsMono  = transactionServiceRepository.findByUniqueId(uniqueId);

        return Mono.zip(  transactionDetailsMono , toTransactionUserMono , fromTransactionUserMono ).flatMap(
                data ->{

                    ArrayList<TransactionRequest> toAcceptedBorrowRequests = data.getT2().getAcceptedBorrowRequests();
                    ArrayList<TransactionRequest> toPendingBorrowRequests  = data.getT2().getPendingBorrowRequests();
                    ArrayList<TransactionRequest> toBorrowRequests = data.getT2().getBorrowRequests();

                    //setting email and name
                    TransactionRequest toTransactionRequest =  new TransactionRequest(
                            uniqueId,
                            data.getT3().getFirstName(),
                            data.getT3().getEmail(),
                            data.getT1().getAmount(),
                            data.getT1().getDays(),
                            data.getT1().getInterest()
                    );

                    toAcceptedBorrowRequests.add(toTransactionRequest);
                    toPendingBorrowRequests.remove(toTransactionRequest);
                    toBorrowRequests.remove(toTransactionRequest);

                    ArrayList<TransactionRequest> fromAcceptedBorrowRequests = data.getT3().getAcceptedBorrowRequests();
                    ArrayList<TransactionRequest> fromPendingBorrowRequests = data.getT3().getPendingBorrowRequests();
                    ArrayList<TransactionRequest> fromBorrowRequests = data.getT3().getBorrowRequests();

                    //setting email and name
                    TransactionRequest fromTransactionRequest =  new TransactionRequest(
                            uniqueId,
                            data.getT2().getFirstName(),
                            data.getT2().getEmail(),
                            data.getT1().getAmount(),
                            data.getT1().getDays(),
                            data.getT1().getInterest()
                    );

                    fromAcceptedBorrowRequests.add(fromTransactionRequest);
                    fromBorrowRequests.remove(fromTransactionRequest);
                    fromPendingBorrowRequests.remove(fromTransactionRequest);

                    return transactionServiceRepositoryTransactionUser.save(data.getT2())
                            .zipWith( transactionServiceRepositoryTransactionUser.save(data.getT3()) , ( to , from ) -> from )
                            .flatMap(
                                    transactionUser -> ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .body( Mono.just(transactionUser) , TransactionUser.class )
                            );

                }
        ).switchIfEmpty(
                ServerResponse.ok().body( Mono.just( new ErrorResponse("error no such users") ) , ErrorResponse.class )
        );

    }

    public Mono<ServerResponse> removeBorrowRequest( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");

        String toEmail = serverRequest.pathVariable("toEmail");

        String uniqueId = serverRequest.pathVariable("uniqueId");

        Mono<TransactionUser> toTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(toEmail);

        Mono<TransactionUser> fromTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(fromEmail);

        Mono<TransactionDetails> transactionDetailsMono  = transactionServiceRepository.findByUniqueId(uniqueId);

        return Mono.zip(  transactionDetailsMono , toTransactionUserMono , fromTransactionUserMono ).flatMap(
                data ->{

                    //setting email and name
                    TransactionRequest toTransactionRequest =  new TransactionRequest(
                            uniqueId,
                            data.getT3().getFirstName(),
                            data.getT3().getEmail(),
                            data.getT1().getAmount(),
                            data.getT1().getDays(),
                            data.getT1().getInterest()
                    );

                    ArrayList<TransactionRequest> toBorrowRequests = data.getT2().getBorrowRequests();
                    toBorrowRequests.remove(toTransactionRequest);
                    data.getT2().setBorrowRequests(toBorrowRequests);

                    //setting emails and name
                    TransactionRequest fromTransactionRequest =  new TransactionRequest(
                            uniqueId,
                            data.getT2().getFirstName(),
                            data.getT2().getEmail(),
                            data.getT1().getAmount(),
                            data.getT1().getDays(),
                            data.getT1().getInterest()
                    );

                    ArrayList<TransactionRequest> fromPendingBorrowRequests = data.getT3().getPendingBorrowRequests();
                    fromPendingBorrowRequests.remove(fromTransactionRequest);
                    data.getT3().setPendingBorrowRequests(fromPendingBorrowRequests);

                    return Mono.zip( transactionServiceRepositoryTransactionUser.save(data.getT2()) ,
                                    transactionServiceRepositoryTransactionUser.save(data.getT3()) ,
                                    transactionServiceRepository.deleteByUniqueId(uniqueId)  )
                            .flatMap(
                                    updatedData -> ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .body( Mono.just(updatedData.getT2()).log() , TransactionUser.class )
                            ).log();

                }
        ).switchIfEmpty(
                ServerResponse.ok().body( Mono.just( new ErrorResponse("error no such users") ) , ErrorResponse.class )
        );

    }

    public Mono<ServerResponse> removeAcceptedBorrowRequests( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");

        String toEmail = serverRequest.pathVariable("toEmail");

        String uniqueId = serverRequest.pathVariable("uniqueId");

        Mono<TransactionUser> toTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(toEmail);

        Mono<TransactionUser> fromTransactionUserMono = transactionServiceRepositoryTransactionUser.findByEmail(fromEmail);

        Mono<TransactionDetails> transactionDetailsMono  = transactionServiceRepository.findByUniqueId(uniqueId);

        return Mono.zip(  transactionDetailsMono , toTransactionUserMono , fromTransactionUserMono ).flatMap(
                data ->{

                    ArrayList<TransactionRequest> toAcceptedBorrowRequests = data.getT2().getAcceptedBorrowRequests();

                    //setting email and name
                    TransactionRequest toTransactionRequest =  new TransactionRequest(
                            uniqueId,
                            data.getT3().getFirstName(),
                            data.getT3().getEmail(),
                            data.getT1().getAmount(),
                            data.getT1().getDays(),
                            data.getT1().getInterest()
                    );

                    toAcceptedBorrowRequests.remove(toTransactionRequest);
                    data.getT2().setAcceptedBorrowRequests(toAcceptedBorrowRequests);


                    ArrayList<TransactionRequest> fromAcceptedBorrowRequests = data.getT3().getAcceptedBorrowRequests();

                    //setting emails
                    TransactionRequest fromTransactionRequest =  new TransactionRequest(
                            uniqueId,
                            data.getT2().getFirstName(),
                            data.getT2().getEmail(),
                            data.getT1().getAmount(),
                            data.getT1().getDays(),
                            data.getT1().getInterest()
                    );

                    fromAcceptedBorrowRequests.remove(fromTransactionRequest);
                    data.getT3().setAcceptedBorrowRequests(fromAcceptedBorrowRequests);

                    return Mono.zip( transactionServiceRepositoryTransactionUser.save(data.getT2()) ,
                             transactionServiceRepositoryTransactionUser.save(data.getT3()) ,
                                  transactionServiceRepository.deleteByUniqueId(uniqueId)  )
                            .flatMap(
                                    updatedData -> ServerResponse.ok()
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .body( Mono.just(updatedData.getT2()) , TransactionUser.class )
                            );

                }
        ).switchIfEmpty(
                ServerResponse.ok().body( Mono.just( new ErrorResponse("error no such users") ) , ErrorResponse.class )
        );

    }

    public Mono<ServerResponse> payAmount( ServerRequest serverRequest ){

        String fromEmail = serverRequest.pathVariable("fromEmail");

        String toEmail = serverRequest.pathVariable("toEmail");

        String uniqueId = serverRequest.pathVariable("uniqueId");

        Mono<PayRequest>  payRequestMono = serverRequest.bodyToMono(PayRequest.class).log();

        Mono<TransactionDetails> transactionDetailsMono  = transactionServiceRepository.findByUniqueId(uniqueId);

        return Mono.zip(  transactionDetailsMono , payRequestMono  ).flatMap(
                data ->{

                    Double amountPayed = data.getT1().getAmountPayed();
                    Double amountPayedBack = data.getT1().getAmountPayedBack();

                    if( data.getT1().getTransactionId().getFromEmail().equals(toEmail) &&
                            data.getT1().getTransactionId().getToEmail().equals(fromEmail) ){

                        amountPayed+=data.getT2().getAmount();

                        if( data.getT1().getAmountPayed() == 0 ){

                            data.getT1().setAmountPayed(amountPayed);

                            return Mono.zip( webClient.post().bodyValue(data.getT1())
                                    .retrieve().bodyToMono(JobRunData.class)
                           , transactionServiceRepository.save(data.getT1()) ).flatMap(
                                            savedData -> ServerResponse.ok().body( Mono.just(savedData.getT1()),
                                                    JobRunData.class )
                                    ).switchIfEmpty(
                                            ServerResponse
                                                    .ok()
                                                    .body( Mono.just( new ErrorResponse("error scheduling job") ),
                                                            ErrorResponse.class )
                                    );

                        }
                        else{

                            return ServerResponse.ok()
                                    .body( Mono.just("error the payment should be paid once")
                                    , ErrorResponse.class );

                        }

                    }
                    else if( data.getT1().getTransactionId().getFromEmail().equals(fromEmail) &&
                            data.getT1().getTransactionId().getToEmail().equals(toEmail) ){

                        amountPayedBack+=data.getT2().getAmount();
                        data.getT1().setAmountPayedBack(amountPayedBack);

                    }

                   return transactionServiceRepository.save(data.getT1())
                           .flatMap(
                           transactionDetails ->  ServerResponse.ok()
                                   .body( Mono.just( data.getT1() ) , TransactionDetails.class )
                   );

                }
        ).switchIfEmpty(
                ServerResponse.ok()
                        .body( Mono.just( new ErrorResponse("error doing transactions") ) , ErrorResponse.class )
        );

    }

}
