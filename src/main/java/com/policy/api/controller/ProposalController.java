package com.policy.api.controller;

import com.policy.api.dto.request.ProposalRequest;
import com.policy.api.dto.response.ProposalResponse;
import com.policy.api.service.ProposalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @PostMapping
    public ProposalResponse createProposal(@RequestBody ProposalRequest proposalRequest) {
        return proposalService.createProposal(proposalRequest);
    }

    @GetMapping("/{proposalId}")
    public ProposalResponse getProposal(@PathVariable String proposalId) {
        return proposalService.getProposal(proposalId);
    }

    @PostMapping("/{proposalId}/submit")
    public ProposalResponse submitProposal(@PathVariable String proposalId) {
        return proposalService.submitProposal(proposalId);
    }
}