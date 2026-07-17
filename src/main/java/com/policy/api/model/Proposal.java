package com.policy.api.model;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.PolicyStatus;
import com.policy.api.constants.PolicyTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Proposal {
    private String proposalId;
    private String customerId;
    private PolicyTerm policyTerm;
    private int sumAssured;
    private String PAN;
    private String Nominee;
    private PaymentFrequency paymentFrequency;
    private int PolicyUid;
    private PolicyStatus policyStatus;
    private boolean deleted;
    private LocalDateTime deletedAt;

}
