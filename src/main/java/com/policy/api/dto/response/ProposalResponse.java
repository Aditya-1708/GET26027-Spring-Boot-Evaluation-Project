package com.policy.api.dto.response;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.PolicyStatus;
import com.policy.api.constants.PolicyTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalResponse {

    private String proposalId;
    private String customerId;
    private PolicyTerm policyTerm;
    private int sumAssured;
    private String PAN;
    private String nominee;
    private PaymentFrequency paymentFrequency;
    private int policyUid;
    private PolicyStatus policyStatus;
}