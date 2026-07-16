package com.policy.api.exception;

public class ProposalAlreadySubmittedException extends ApiException {

    public ProposalAlreadySubmittedException(String proposalId) {
        super("Proposal with ID " + proposalId + " has already been submitted.");
    }
}