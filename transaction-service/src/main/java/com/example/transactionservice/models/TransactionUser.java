package com.example.transactionservice.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document( collection = "transactionUser")
public class TransactionUser {

    @Id
    String email;

    String firstName;

    String lastName;

    ArrayList<TransactionRequest> borrowRequests;

    ArrayList<TransactionRequest> pendingBorrowRequests;

    ArrayList<TransactionRequest> acceptedBorrowRequests;

}
