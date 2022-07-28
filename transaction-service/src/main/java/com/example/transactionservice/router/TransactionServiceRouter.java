package com.example.transactionservice.router;

import com.example.transactionservice.handler.TransactionServiceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class TransactionServiceRouter {

    @Bean
    public RouterFunction<ServerResponse> route( TransactionServiceHandler transactionServiceHandler ){
        return  RouterFunctions.route( POST("/transaction/user").and(accept(MediaType.APPLICATION_JSON)),transactionServiceHandler::initTransactionUser )
                .andRoute( GET("/transaction/{uniqueId}"), transactionServiceHandler::getTransaction )
                .andRoute( GET("/transaction/all/{fromEmail}/{toEmail}"), transactionServiceHandler::getTransactions )
                .andRoute( GET("/transaction/user/details/{email}"),transactionServiceHandler::getUserTransactions )

                .andRoute( POST("/transaction/send/borrow/request/{fromEmail}/{toEmail}"), transactionServiceHandler::sendBorrowRequest )
                .andRoute( GET("/transaction/accept/borrow/request/{fromEmail}/{toEmail}/{uniqueId}"),transactionServiceHandler::acceptBorrowRequests )
                .andRoute( GET("/transaction/remove/borrow/request/{fromEmail}/{toEmail}/{uniqueId}"), transactionServiceHandler::removeBorrowRequest )
                .andRoute( GET("/transaction/remove/accepted/borrow/request/{fromEmail}/{toEmail}/{uniqueId}"),transactionServiceHandler::removeAcceptedBorrowRequests )

                .andRoute( POST("/transaction/pay/{fromEmail}/{toEmail}/{uniqueId}").and(accept(MediaType.APPLICATION_JSON)) , transactionServiceHandler::payAmount )

                .andRoute( GET("/transaction/") , transactionServiceHandler::test );

    }

}
