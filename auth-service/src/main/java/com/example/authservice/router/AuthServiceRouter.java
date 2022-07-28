package com.example.authservice.router;

import com.example.authservice.handler.AuthServiceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AuthServiceRouter {

    @Bean
    public RouterFunction<ServerResponse> route( AuthServiceHandler handlerFunction){
        return RouterFunctions.route(POST("/auth/register/{email}").and(accept(MediaType.APPLICATION_JSON)),handlerFunction::registerUser)
                .andRoute(POST("/auth/login/{email}").and(accept(MediaType.APPLICATION_JSON)),handlerFunction::loginUser)
                .andRoute(GET("/auth/user/{email}"),handlerFunction::getUser);
    }

}
