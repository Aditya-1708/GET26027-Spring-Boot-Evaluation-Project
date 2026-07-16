package com.policy.api.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AuditResponse {
    private String auditId;
    private String proposalId;
    private String action;
    private LocalDateTime timestamp;
}
