package com.policy.api.exception;

public class ProposalNotFoundException extends ApiException {

    public ProposalNotFoundException(String id) {
        super("Proposal with id " + id + " not found.");
    }

}
