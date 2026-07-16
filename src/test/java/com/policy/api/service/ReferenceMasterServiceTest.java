package com.policy.api.service;

import com.policy.api.constants.PaymentFrequency;
import com.policy.api.constants.ReferenceCategory;
import com.policy.api.dto.response.ReferenceDataResponse;
import com.policy.api.repository.ReferenceMasterRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReferenceMasterServiceTest {

    @Mock
    private ReferenceMasterRepository repository;

    @InjectMocks
    private ReferenceMasterService service;

    @Test
    void shouldReturnPolicyTerms() {

        List<Integer> policyTerms = List.of(10, 15, 20, 25, 30);

        when(repository.getPolicyTerms())
                .thenReturn(policyTerms);

        ReferenceDataResponse<?> response =
                service.getReferenceData(ReferenceCategory.POLICY_TERM);

        assertNotNull(response);
        assertEquals("POLICY_TERM", response.getCategory());
        assertEquals(policyTerms, response.getValues());

        verify(repository, times(1)).getPolicyTerms();
    }

    @Test
    void shouldReturnPaymentFrequencies() {

        List<PaymentFrequency> frequencies = List.of(
                PaymentFrequency.MONTHLY,
                PaymentFrequency.QUARTERLY,
                PaymentFrequency.HALF_YEARLY,
                PaymentFrequency.YEARLY
        );

        when(repository.getPaymentFrequencies())
                .thenReturn(frequencies);

        ReferenceDataResponse<?> response =
                service.getReferenceData(
                        ReferenceCategory.PAYMENT_FREQUENCY);

        assertNotNull(response);
        assertEquals("PAYMENT_FREQUENCY", response.getCategory());
        assertEquals(frequencies, response.getValues());

        verify(repository, times(1))
                .getPaymentFrequencies();
    }
}