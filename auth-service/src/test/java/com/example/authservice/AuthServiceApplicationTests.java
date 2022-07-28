package com.example.authservice;

import com.example.authservice.models.PayUser;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static com.example.authservice.utils.Role.BORROWER;
import static com.example.authservice.utils.Role.LENDER;

@RunWith(SpringRunner.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuthServiceApplicationTests {

        @Autowired
        WebTestClient webTestClient;

        @Test
        public void AddUserToAuthServiceAndUserService(){

            MultiValueMap<String, String> bodyValues = new LinkedMultiValueMap<>();

            bodyValues.add("email","likith@gamil.com");
            bodyValues.add("password","12345");
            bodyValues.add("firstName","ram");
            bodyValues.add("lastName","rom");
            bodyValues.add("roles", List.of(BORROWER,LENDER).toString());
            bodyValues.add("phoneNumber","8008905428");

            webTestClient.put()
                    .uri("htpp://localhost:9001/auth/register")
                    .accept(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromFormData(bodyValues))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(PayUser.class)
                   .hasSize(1);


    }
}
