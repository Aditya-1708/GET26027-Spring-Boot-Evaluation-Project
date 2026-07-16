package com.policy.api.service;

import com.policy.api.constants.PolicyStatus;
import com.policy.api.constants.PolicyTerm;
import com.policy.api.dto.request.AuditRequest;
import com.policy.api.dto.request.ProposalRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.dto.response.ProposalResponse;
import com.policy.api.model.Proposal;
import com.policy.api.repository.ProposalRepository;
import com.policy.api.util.IdGenerator;
import com.policy.api.validation.Validation;
import org.springframework.stereotype.Service;

@Service
public class ProposalSerivce {

    private final ProposalRepository repository;
    private final Validation validation;
    private final IdGenerator generator;
    private final CustomerService customerService;
    private final AuditService auditService;

    public ProposalSerivce(ProposalRepository repository, IdGenerator generator, Validation validation, CustomerService customerService, AuditService auditService) {
        this.repository = repository;
        this.generator = generator;
        this.validation = validation;
        this.customerService = customerService;
        this.auditService = auditService;
    }

    private ProposalResponse mapToResponse(Proposal proposal) {
        return new ProposalResponse(proposal.getProposalId(), proposal.getCustomerId(), proposal.getPolicyTerm(), proposal.getSumAssured(), proposal.getPAN(), proposal.getNominee(), proposal.getPaymentFrequency(), proposal.getPolicyUid(), proposal.getPolicyStatus());
    }

    public ProposalResponse createProposal(ProposalRequest proposal) {
        String isValid = validation.validateProposal(proposal);

        CustomerResponse customer = customerService.getCustomer(proposal.getCustomerId());

        String customerName = customer.getFirstName() + " " + customer.getLastName();

        if (customerName.equalsIgnoreCase(proposal.getNominee())) {
            throw new RuntimeException("Customer and nominee cannot be the same.");
        }
        if (!(isValid.equals("true"))) {
            throw new RuntimeException(isValid);
        }
        Proposal newProposal = new Proposal(generator.generateProposalId(), proposal.getCustomerId(), PolicyTerm.fromValue(proposal.getPolicyTerm()), proposal.getSumAssured(), proposal.getPAN(), proposal.getNominee(), proposal.getPaymentFrequency(), 0, PolicyStatus.PENDING);

        Proposal savedProposal = repository.save(newProposal);

        return mapToResponse(savedProposal);

    }

    public ProposalResponse getProposal(String proposalId) {
        Proposal fetchedProposal = repository.get(proposalId);

        return mapToResponse(fetchedProposal);
    }

    public ProposalResponse submitProposal(String proposalId) {

        Proposal proposal = repository.get(proposalId);

        if (proposal == null) {
            throw new RuntimeException("Proposal not found");
        }

        proposal.setPolicyUid(generator.generatePolicyNumber());
        proposal.setPolicyStatus(PolicyStatus.ACCEPTED);

        Proposal updatedProposal = repository.save(proposal);

        auditService.createAudit(new AuditRequest(generator.generateAuditId(), updatedProposal.getProposalId(), "Proposal submitted successfully"));

        return mapToResponse(updatedProposal);
    }
}
