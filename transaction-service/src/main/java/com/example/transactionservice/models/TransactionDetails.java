package com.example.transactionservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document( collection = "transaction")
public class TransactionDetails {
    @Id
    String uniqueId;
    String fromName;
    String toName;
    TransactionId transactionId;
    Double amount;
    Double amountPayed;
    Double amountPayedBack;
    Long days;
    float interest;
}
