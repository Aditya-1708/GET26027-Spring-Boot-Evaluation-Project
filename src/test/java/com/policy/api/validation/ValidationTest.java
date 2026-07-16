package com.policy.api.validation;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.dto.request.CustomerRequest;
import com.policy.api.dto.request.ProposalRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationTest {

    private Validation validation;

    @BeforeEach
    void setUp() {
        validation = new Validation();
    }

    @Test
    void shouldValidateCustomerSuccessfully() {

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        assertEquals(
                "true",
                validation.validateCustomer(request)
        );
    }

    @Test
    void shouldFailWhenCustomerAgeIsLessThan18() {

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                17,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        assertEquals(
                "Customer age must be between 18 and 65 years.",
                validation.validateCustomer(request)
        );
    }

    @Test
    void shouldFailWhenCustomerAgeIsGreaterThan65() {

        CustomerRequest request = new CustomerRequest(
                "Aditya",
                "Umesh",
                70,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore"
        );

        assertEquals(
                "Customer age must be between 18 and 65 years.",
                validation.validateCustomer(request)
        );
    }

    @Test
    void shouldValidateProposalSuccessfully() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                500000,
                60000,
                "ABCDE1234F",
                "Rahul Sharma",
                PaymentFrequency.YEARLY
        );

        assertEquals(
                "true",
                validation.validateProposal(request)
        );
    }

    @Test
    void shouldFailForInvalidPolicyTerm() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                18,
                500000,
                60000,
                "ABCDE1234F",
                "Rahul Sharma",
                PaymentFrequency.YEARLY
        );

        assertEquals(
                "Invalid policy term",
                validation.validateProposal(request)
        );
    }

    @Test
    void shouldFailForInvalidSumAssured() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                90000,
                60000,
                "ABCDE1234F",
                "Rahul Sharma",
                PaymentFrequency.YEARLY
        );

        assertEquals(
                "Assured Sum is not in the recommended range",
                validation.validateProposal(request)
        );
    }

    @Test
    void shouldFailForInvalidPremium() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                500000,
                3000,
                "",
                "Rahul Sharma",
                PaymentFrequency.YEARLY
        );

        assertEquals(
                "premium less than minimum requirement",
                validation.validateProposal(request)
        );
    }

    @Test
    void shouldFailForInvalidPan() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                500000,
                60000,
                "ABCDE123",
                "Rahul Sharma",
                PaymentFrequency.YEARLY
        );

        assertEquals(
                "PAN number is mandatory for policies with annual premium of 50000",
                validation.validateProposal(request)
        );
    }
}