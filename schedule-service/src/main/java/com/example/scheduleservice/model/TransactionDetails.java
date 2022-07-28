package com.example.scheduleservice.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetails {
    String UniqueId;
    String fromName;
    String toName;
    TransactionId transactionId;
    Double amount;

    Double amountPayed;

    Double amountPayedBack;

    Long days;
    float interest;
}
