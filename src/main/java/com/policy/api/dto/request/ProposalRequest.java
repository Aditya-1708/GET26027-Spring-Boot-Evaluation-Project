package com.policy.api.dto.request;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.PolicyTerm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProposalRequest {
    private String customerId;
    private int policyTerm;
    private int sumAssured;
    private int premium;
    private String PAN;
    private String Nominee;
    private PaymentFrequency paymentFrequency;
}
