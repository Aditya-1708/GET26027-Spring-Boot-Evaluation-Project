package com.policy.api.repository;

import com.policy.api.model.Proposal;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ProposalRepository {

    private final Map<String, Proposal> map;

    public ProposalRepository() {
        this.map = new ConcurrentHashMap<>();
    }

    public Proposal save(Proposal proposal) {
        map.put(proposal.getProposalId(), proposal);
        return proposal;
    }

    public Proposal get(String proposalId) {
        return map.getOrDefault(proposalId, null);
    }

    public List<Proposal> getByCustomerId(String customerId) {
        return map.values().stream()
                .filter(proposal -> proposal.getCustomerId().equals(customerId)).toList();
    }


}