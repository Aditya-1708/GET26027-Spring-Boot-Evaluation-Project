package com.policy.api.service;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.PolicyStatus;
import com.policy.api.constants.PolicyTerm;
import com.policy.api.dto.request.AuditRequest;
import com.policy.api.dto.request.ProposalRequest;
import com.policy.api.dto.response.AuditResponse;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.dto.response.ProposalResponse;
import com.policy.api.exception.CustomerNotFoundException;
import com.policy.api.exception.InvalidProposalException;
import com.policy.api.exception.ProposalNotFoundException;
import com.policy.api.model.Proposal;
import com.policy.api.repository.ProposalRepository;
import com.policy.api.util.IdGenerator;
import com.policy.api.util.MaskPii;
import com.policy.api.validation.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProposalServiceTest {

    @Mock
    private ProposalRepository repository;

    @Mock
    private Validation validation;

    @Mock
    private IdGenerator generator;

    @Mock
    private CustomerService customerService;

    @Mock
    private AuditService auditService;

    @Spy
    private MaskPii maskPii = new MaskPii();

    @InjectMocks
    private ProposalService service;

    @Test
    void shouldCreateProposalSuccessfully() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                500000,
                60000,
                "ABCDE1234F",
                "Rahul Sharma",
                PaymentFrequency.YEARLY);

        CustomerResponse customer = new CustomerResponse(
                "CUST001",
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore");

        Proposal proposal = new Proposal(
                "PROP001",
                "CUST001",
                PolicyTerm.TERM_20,
                500000,
                "ABCDE1234F",
                "Rahul Sharma",
                PaymentFrequency.YEARLY,
                0,
                PolicyStatus.PENDING,
                false,
                null);

        when(validation.validateProposal(request)).thenReturn("true");
        when(customerService.getCustomer("CUST001")).thenReturn(customer);
        when(generator.generateProposalId()).thenReturn("PROP001");

        when(repository.save(any(Proposal.class)))
                .thenReturn(proposal);

        ProposalResponse response = service.createProposal(request);

        assertAll(
                () -> assertNotNull(response),
                () -> assertEquals("PROP001", response.getProposalId()),
                () -> assertEquals("CUST001", response.getCustomerId()),
                () -> assertEquals(PolicyTerm.TERM_20, response.getPolicyTerm()),
                () -> assertEquals(500000, response.getSumAssured()),
                () -> assertEquals("ABCDE****F", response.getPAN()),
                () -> assertEquals("Rahul Sharma", response.getNominee()),
                () -> assertEquals(PaymentFrequency.YEARLY, response.getPaymentFrequency()),
                () -> assertEquals(0, response.getPolicyUid()),
                () -> assertEquals(PolicyStatus.PENDING, response.getPolicyStatus())
        );

        verify(validation).validateProposal(request);
        verify(customerService).getCustomer("CUST001");
        verify(generator).generateProposalId();
        verify(repository).save(any(Proposal.class));    }

    @Test
    void shouldThrowInvalidProposalExceptionWhenValidationFails() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                18,
                500000,
                60000,
                "ABCDE1234F",
                "Rahul",
                PaymentFrequency.YEARLY);

        when(validation.validateProposal(request))
                .thenReturn("Invalid policy term");

        assertThrows(
                InvalidProposalException.class,
                () -> service.createProposal(request));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowCustomerNotFoundException() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                500000,
                60000,
                "ABCDE1234F",
                "Rahul",
                PaymentFrequency.YEARLY);

        when(validation.validateProposal(request)).thenReturn("true");

        when(customerService.getCustomer("CUST001"))
                .thenThrow(new CustomerNotFoundException("CUST001"));

        assertThrows(
                CustomerNotFoundException.class,
                () -> service.createProposal(request));
    }

    @Test
    void shouldThrowExceptionWhenNomineeIsCustomer() {

        ProposalRequest request = new ProposalRequest(
                "CUST001",
                20,
                500000,
                60000,
                "ABCDE1234F",
                "Aditya Umesh",
                PaymentFrequency.YEARLY);

        CustomerResponse customer = new CustomerResponse(
                "CUST001",
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore");

        when(validation.validateProposal(request)).thenReturn("true");
        when(customerService.getCustomer("CUST001")).thenReturn(customer);

        assertThrows(
                InvalidProposalException.class,
                () -> service.createProposal(request));
    }

    @Test
    void shouldReturnProposal() {

        Proposal proposal = new Proposal(
                "PROP001",
                "CUST001",
                PolicyTerm.TERM_20,
                500000,
                "ABCDE1234F",
                "Rahul",
                PaymentFrequency.YEARLY,
                0,
                PolicyStatus.PENDING,
                false,
                null);

        CustomerResponse customer = new CustomerResponse(
                "CUST001",
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore");

        when(repository.get("PROP001"))
                .thenReturn(proposal);
        when(customerService.getCustomer("CUST001")).thenReturn(customer);

        ProposalResponse response = service.getProposal("PROP001");

        assertEquals("PROP001", response.getProposalId());
    }

    @Test
    void shouldThrowProposalNotFoundException() {

        when(repository.get("PROP999"))
                .thenReturn(null);

        assertThrows(
                ProposalNotFoundException.class,
                () -> service.getProposal("PROP999"));
    }

    @Test
    void shouldSubmitProposalSuccessfully() {

        Proposal proposal = new Proposal(
                "PROP001",
                "CUST001",
                PolicyTerm.TERM_20,
                500000,
                "ABCDE1234F",
                "Rahul",
                PaymentFrequency.YEARLY,
                0,
                PolicyStatus.PENDING,
                false,
                null);

        CustomerResponse customer = new CustomerResponse(
                "CUST001",
                "Aditya",
                "Umesh",
                22,
                "Male",
                "9876543210",
                "aditya@gmail.com",
                "Bangalore");

        when(repository.get("PROP001"))
                .thenReturn(proposal);

        when(customerService.getCustomer("CUST001")).thenReturn(customer);

        when(generator.generatePolicyNumber())
                .thenReturn(100001);

        when(repository.save(any(Proposal.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(auditService.createAudit(any(AuditRequest.class)))
                .thenReturn(
                        new AuditResponse(
                                "AUD001",
                                "PROP001",
                                "Proposal submitted successfully",
                                null));

        ProposalResponse response = service.submitProposal("PROP001");

        assertEquals(100001, response.getPolicyUid());
        assertEquals(PolicyStatus.ACCEPTED,
                response.getPolicyStatus());

        verify(auditService).createAudit(any(AuditRequest.class));
    }

    @Test
    void shouldThrowProposalNotFoundWhenSubmitting() {

        when(repository.get("PROP999"))
                .thenReturn(null);

        assertThrows(
                ProposalNotFoundException.class,
                () -> service.submitProposal("PROP999"));
    }

    @Test
    void shouldThrowCustomerNotFoundWhenGetProposalHasDeletedCustomer() {
        Proposal proposal = new Proposal("PROP001", "CUST001", PolicyTerm.TERM_20, 500000, "ABCDE1234F", "Rahul", PaymentFrequency.YEARLY, 0, PolicyStatus.PENDING, false, null);

        when(repository.get("PROP001")).thenReturn(proposal);
        when(customerService.getCustomer("CUST001")).thenThrow(new CustomerNotFoundException("CUST001"));

        assertThrows(CustomerNotFoundException.class, () -> service.getProposal("PROP001"));
    }

    @Test
    void shouldThrowCustomerNotFoundWhenSubmitProposalHasDeletedCustomer() {
        Proposal proposal = new Proposal("PROP001", "CUST001", PolicyTerm.TERM_20, 500000, "ABCDE1234F", "Rahul", PaymentFrequency.YEARLY, 0, PolicyStatus.PENDING, false, null);

        when(repository.get("PROP001")).thenReturn(proposal);
        when(customerService.getCustomer("CUST001")).thenThrow(new CustomerNotFoundException("CUST001"));

        assertThrows(CustomerNotFoundException.class, () -> service.submitProposal("PROP001"));
    }

    @Test
    void shouldThrowCustomerNotFoundWhenDeleteProposalHasDeletedCustomer() {
        Proposal proposal = new Proposal("PROP001", "CUST001", PolicyTerm.TERM_20, 500000, "ABCDE1234F", "Rahul", PaymentFrequency.YEARLY, 0, PolicyStatus.PENDING, false, null);

        when(repository.get("PROP001")).thenReturn(proposal);
        when(customerService.getCustomer("CUST001")).thenThrow(new CustomerNotFoundException("CUST001"));

        assertThrows(CustomerNotFoundException.class, () -> service.deleteProposal("PROP001"));
    }

    @Test
    void shouldDeleteProposalSuccessfully() {
        Proposal proposal = new Proposal("PROP001", "CUST001", PolicyTerm.TERM_20, 500000, "ABCDE1234F", "Rahul", PaymentFrequency.YEARLY, 0, PolicyStatus.PENDING, false, null);
        CustomerResponse customer = new CustomerResponse("CUST001", "Aditya", "Umesh", 22, "Male", "9876543210", "aditya@gmail.com", "Bangalore");

        when(repository.get("PROP001")).thenReturn(proposal);
        when(customerService.getCustomer("CUST001")).thenReturn(customer);
        when(repository.save(any(Proposal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProposalResponse response = service.deleteProposal("PROP001");

        assertNotNull(response);
    }

    @Test
    void shouldThrowInvalidProposalExceptionWhenDeletingSubmittedProposal() {
        Proposal proposal = new Proposal("PROP001", "CUST001", PolicyTerm.TERM_20, 500000, "ABCDE1234F", "Rahul", PaymentFrequency.YEARLY, 100001, PolicyStatus.ACCEPTED, false, null);
        CustomerResponse customer = new CustomerResponse("CUST001", "Aditya", "Umesh", 22, "Male", "9876543210", "aditya@gmail.com", "Bangalore");

        when(repository.get("PROP001")).thenReturn(proposal);
        when(customerService.getCustomer("CUST001")).thenReturn(customer);

        assertThrows(InvalidProposalException.class, () -> service.deleteProposal("PROP001"));
    }
}