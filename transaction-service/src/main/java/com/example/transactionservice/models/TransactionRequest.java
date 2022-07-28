package com.example.transactionservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TransactionRequest {
    String uniqueId;
    String name;
    String email;
    Double amount;
    Long days;
    float interest;
}
