package com.policy.api.repository;

import com.policy.api.model.Proposal;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProposalRepository {

    private final Map<String, Proposal> map;

    public ProposalRepository() {
        this.map = new HashMap<>();
    }

    public Proposal save(Proposal proposal) {
        map.put(proposal.getProposalId(), proposal);
        return proposal;
    }

    public Proposal get(String proposalId) {
        return map.getOrDefault(proposalId, null);
    }


}