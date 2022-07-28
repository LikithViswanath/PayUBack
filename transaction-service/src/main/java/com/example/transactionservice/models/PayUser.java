package com.example.transactionservice.models;


import com.example.transactionservice.utils.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Document( collection = "users")
public class PayUser implements Serializable {

    @Id
    private ObjectId id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private List<Role> roles;
    private BigInteger phoneNumber;

}
