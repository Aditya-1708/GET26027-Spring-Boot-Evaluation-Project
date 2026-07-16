package com.policy.api.service;

import com.policy.api.constants.ReferenceCategory;
import com.policy.api.dto.response.ReferenceDataResponse;
import com.policy.api.repository.ReferenceMasterRepository;
import org.springframework.stereotype.Service;

@Service
public class ReferenceMasterService {

    private final ReferenceMasterRepository repository;

    public ReferenceMasterService(ReferenceMasterRepository repository) {
        this.repository = repository;
    }

    public ReferenceDataResponse<?> getReferenceData(ReferenceCategory category) {

        return switch (category) {

            case POLICY_TERM ->
                    new ReferenceDataResponse<>(
                            "POLICY_TERM",
                            repository.getPolicyTerms()
                    );

            case PAYMENT_FREQUENCY ->
                    new ReferenceDataResponse<>(
                            "PAYMENT_FREQUENCY",
                            repository.getPaymentFrequencies()
                    );
        };
    }
}