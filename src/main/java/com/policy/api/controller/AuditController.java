package com.policy.api.controller;

import com.policy.api.dto.response.AuditResponse;
import com.policy.api.service.AuditService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/audits")
public class AuditController {
    private final AuditService service;

    public AuditController(AuditService service){
        this.service = service;
    }

    @GetMapping
    public List<AuditResponse> getAllAudits(){
        return service.getAudits();
    }
}
