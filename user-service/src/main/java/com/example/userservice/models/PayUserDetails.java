package com.example.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.ArrayList;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document(collection = "userDetails")
public class PayUserDetails implements Serializable {

    @Id
    String email;
    ArrayList<String> pendingRequests;
    ArrayList<String> connections;
    ArrayList<String> connectionRequests;

}
