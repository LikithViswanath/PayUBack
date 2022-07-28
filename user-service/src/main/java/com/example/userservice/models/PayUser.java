package com.example.userservice.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
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
