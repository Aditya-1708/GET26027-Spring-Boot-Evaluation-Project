package com.policy.api.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class Audit {
    private String auditId;
    private String proposalId;
    private String action;
    private LocalDateTime timestamp;
}
