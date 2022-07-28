package com.example.userservice.router;

import com.example.userservice.handler.UserServiceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class UserServiceRouter {

    @Bean
    public RouterFunction<ServerResponse> route(UserServiceHandler userServiceHandler){
        return RouterFunctions.route(GET("/user/connect/{fromEmail}/{toEmail}"), userServiceHandler::sendConnectionRequest)
                .andRoute(GET("/user/remove/connection/{fromEmail}/{toEmail}"),userServiceHandler::removeConnection)
                .andRoute(GET("/user/accept/request/{fromEmail}/{toEmail}"),userServiceHandler::acceptConnectionRequest)
                .andRoute(GET("/user/remove/request/{fromEmail}/{toEmail}"), userServiceHandler::removeConnectionRequest)
                .andRoute(GET("/user/details/{email}"),userServiceHandler::userDetails)
                .andRoute(POST("/user/").and(accept(MediaType.APPLICATION_JSON)),userServiceHandler::initUser);
    }

}
