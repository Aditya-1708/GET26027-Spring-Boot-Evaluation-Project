package com.policy.api.dto.request;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.PolicyTerm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProposalRequest {

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @Min(10)
    private int policyTerm;

    @Positive
    private int sumAssured;

    @Positive
    private int premium;

    private String PAN;

    @NotBlank(message = "Nominee is required")
    private String nominee;

    @NotNull(message = "Payment frequency is required")
    private PaymentFrequency paymentFrequency;
}