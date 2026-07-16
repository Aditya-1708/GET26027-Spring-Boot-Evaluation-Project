package com.policy.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class CustomerResponse {
    private String customerId;
    private String firstName;
    private String lastName;
    private int age;
    private String gender;
    private String mobileNumber;
    private String email;
    private String address;
}
