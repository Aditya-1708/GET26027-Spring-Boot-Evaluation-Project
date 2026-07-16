package com.policy.api.controller;

import com.policy.api.constants.ReferenceCategory;
import com.policy.api.service.ReferenceMasterService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reference-master")
public class ReferenceMasterController {

    private final ReferenceMasterService service;

    public ReferenceMasterController(ReferenceMasterService service) {
        this.service = service;
    }

    @GetMapping("/{category}")

    public Object getReferenceData(@PathVariable ReferenceCategory category) {
        return service.getReferenceData(category);
    }

}