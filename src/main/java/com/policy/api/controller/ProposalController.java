package com.policy.api.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/proposals")
public class ProposalController {

    private final proposalService service;

    public ProposalController(proposalService service) {
        this.service = service;
    }

    @PostMapping
    public ProposalResponse createProposal(@RequestBody ProposalRequest proposal){
        return service.createProposal(proposal);
    }

    @GetMapping("/{proposalID}")
    public ProposalResponse getProposal(@PathVariable String proposalID){
        return service.getProposal(proposalID);
    }

    @PostMapping("/{proposalID}/submit")
    public ProposalResponse submitProposal (@PathVariable String proposalID){
        return service.submitProposal(proposalID);
    }
}
