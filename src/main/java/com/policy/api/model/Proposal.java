package com.policy.api.model;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.PolicyTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Proposal {
    private String proposalId;
    private PolicyTerm policyTerm;
    private int sumAssured;
    private String PAN;
    private String Nominee;
    private PaymentFrequency paymentFrequency;
}
