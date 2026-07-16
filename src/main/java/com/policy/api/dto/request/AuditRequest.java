package com.policy.api.dto.request;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AuditRequest {
    private String auditId;
    private String proposalId;
    private String action;
}
