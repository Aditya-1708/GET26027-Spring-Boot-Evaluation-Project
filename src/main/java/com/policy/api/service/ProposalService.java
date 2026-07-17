package com.policy.api.service;

import com.policy.api.constants.PolicyStatus;
import com.policy.api.constants.PolicyTerm;
import com.policy.api.util.MaskPii;
import com.policy.api.dto.request.AuditRequest;
import com.policy.api.dto.request.ProposalRequest;
import com.policy.api.dto.response.CustomerResponse;
import com.policy.api.dto.response.ProposalResponse;
import com.policy.api.exception.InvalidProposalException;
import com.policy.api.exception.ProposalAlreadySubmittedException;
import com.policy.api.exception.ProposalNotFoundException;
import com.policy.api.model.Proposal;
import com.policy.api.repository.ProposalRepository;
import com.policy.api.util.IdGenerator;
import com.policy.api.validation.Validation;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ProposalService {

    private final ProposalRepository repository;
    private final Validation validation;
    private final IdGenerator generator;
    private final CustomerService customerService;
    private final AuditService auditService;
    private final MaskPii maskPii = new MaskPii();

    public ProposalService(
            ProposalRepository repository,
            CustomerService customerService,
            AuditService auditService,
            IdGenerator generator,
            Validation validation) {

        this.repository = repository;
        this.customerService = customerService;
        this.auditService = auditService;
        this.generator = generator;
        this.validation = validation;
    }

    private Proposal getActiveProposal(String proposalId) {
        Proposal proposal = repository.get(proposalId);

        if (proposal == null || proposal.isDeleted()) {
            throw new ProposalNotFoundException(proposalId);
        }

        return proposal;
    }

    private Proposal mapToModel(ProposalRequest proposal) {
        return new Proposal(generator.generateProposalId(), proposal.getCustomerId(), PolicyTerm.fromValue(proposal.getPolicyTerm()), proposal.getSumAssured(), proposal.getPAN(), proposal.getNominee(), proposal.getPaymentFrequency(), 0, PolicyStatus.PENDING, false, null);
    }

    private ProposalResponse mapToResponse(Proposal proposal) {
        return new ProposalResponse(proposal.getProposalId(), proposal.getCustomerId(), proposal.getPolicyTerm(), proposal.getSumAssured(), maskPii.maskPAN(proposal.getPAN()), proposal.getNominee(), proposal.getPaymentFrequency(), proposal.getPolicyUid(), proposal.getPolicyStatus());
    }

    public ProposalResponse createProposal(ProposalRequest proposal) {

        String isValid = validation.validateProposal(proposal);
        if (!"true".equals(isValid)) {
            throw new InvalidProposalException(isValid);
        }

        CustomerResponse customer = customerService.getCustomer(proposal.getCustomerId());

        String customerName = customer.getFirstName() + " " + customer.getLastName();

        if (customerName.equalsIgnoreCase(proposal.getNominee())) {
            throw new InvalidProposalException("Customer and nominee cannot be the same.");
        }

        Proposal newProposal = mapToModel(proposal);

        Proposal savedProposal = repository.save(newProposal);

        return mapToResponse(savedProposal);
    }

    public ProposalResponse getProposal(String proposalId) {

        Proposal fetchedProposal = getActiveProposal(proposalId);

        return mapToResponse(fetchedProposal);
    }

    public ProposalResponse submitProposal(String proposalId) {

        Proposal proposal = getActiveProposal(proposalId);

        if (proposal.getPolicyStatus() == PolicyStatus.ACCEPTED) {
            throw new ProposalAlreadySubmittedException(proposalId);
        }

        proposal.setPolicyUid(generator.generatePolicyNumber());
        proposal.setPolicyStatus(PolicyStatus.ACCEPTED);


        auditService.createAudit(new AuditRequest(proposal.getProposalId(), "Proposal submitted successfully"));

        return mapToResponse(proposal);

    }

    public ProposalResponse deleteProposal(String proposalId) {
        Proposal proposal = getActiveProposal(proposalId);

        if (proposal.getPolicyStatus() == PolicyStatus.ACCEPTED) {
            throw new InvalidProposalException(
                    "Submitted proposals cannot be deleted."
            );
        }

        proposal.setDeleted(true);
        proposal.setDeletedAt(LocalDateTime.now());

        return mapToResponse(proposal);
    }

}
