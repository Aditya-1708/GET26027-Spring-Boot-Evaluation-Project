package com.policy.api.service;

import com.policy.api.repository.CustomerRepository;
import com.policy.api.util.IdGenerator;

public class proposalSerivce {
    private final ProposalRepository repository;

    private final IdGenerator generator;

    public ProposalService(ProposalRepository repository, IdGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    public ProposalResponse createProposal(ProposalRequest proposal){

    }
}
