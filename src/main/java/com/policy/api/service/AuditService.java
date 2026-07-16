package com.policy.api.service;

import com.policy.api.dto.request.AuditRequest;
import com.policy.api.dto.response.AuditResponse;
import com.policy.api.model.Audit;
import com.policy.api.repository.AuditRepository;
import com.policy.api.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditService {

    private final AuditRepository repository;
    private final IdGenerator generator;

    public AuditService(AuditRepository repository, IdGenerator generator) {
        this.repository = repository;
        this.generator = generator;
    }

    private Audit mapToModel(AuditRequest audit) {
        return new Audit(
                generator.generateAuditId(),
                audit.getProposalId(),
                audit.getAction(),
                LocalDateTime.now()
        );
    }

    private AuditResponse mapToResponse(Audit audit) {
        return new AuditResponse(
                audit.getAuditId(),
                audit.getProposalId(),
                audit.getAction(),
                audit.getTimestamp()
        );
    }

    public AuditResponse createAudit(AuditRequest audit) {

        Audit newAudit = mapToModel(audit);
        Audit savedAudit = repository.save(newAudit);

        return mapToResponse(savedAudit);
    }

    public List<AuditResponse> getAudits() {

        List<Audit> audits = repository.get();
        List<AuditResponse> responses = new ArrayList<>();

        for (Audit audit : audits) {
            responses.add(mapToResponse(audit));
        }

        return responses;
    }
}