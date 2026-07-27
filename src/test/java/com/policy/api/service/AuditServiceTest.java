package com.policy.api.service;

import com.policy.api.dto.request.AuditRequest;
import com.policy.api.dto.response.AuditResponse;
import com.policy.api.model.Audit;
import com.policy.api.repository.AuditRepository;
import com.policy.api.util.IdGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditRepository repository;

    @Mock
    private IdGenerator generator;

    @InjectMocks
    private AuditService service;

    @Test
    void shouldCreateAuditSuccessfully() {

        AuditRequest request = new AuditRequest(
                "PROP001",
                "Proposal submitted successfully"
        );

        Audit audit = new Audit(
                "AUD001",
                "PROP001",
                "Proposal submitted successfully",
                LocalDateTime.now()
        );

        when(generator.generateAuditId())
                .thenReturn("AUD001");

        when(repository.save(any(Audit.class)))
                .thenReturn(audit);

        AuditResponse response = service.createAudit(request);

        assertNotNull(response);
        assertEquals("AUD001", response.getAuditId());
        assertEquals("PROP001", response.getProposalId());
        assertEquals("Proposal submitted successfully", response.getAction());

        verify(repository, times(1)).save(any(Audit.class));
    }

    @Test
    void shouldReturnAllAudits() {

        List<Audit> audits = List.of(

                new Audit(
                        "AUD001",
                        "PROP001",
                        "Proposal submitted successfully",
                        LocalDateTime.now()
                ),

                new Audit(
                        "AUD002",
                        "PROP002",
                        "Proposal submitted successfully",
                        LocalDateTime.now()
                )
        );

        when(repository.get()).thenReturn(audits);

        List<AuditResponse> responses = service.getAudits();

        assertEquals(2, responses.size());

        verify(repository, times(1)).get();
    }
}