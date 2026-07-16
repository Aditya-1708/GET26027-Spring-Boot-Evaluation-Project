package com.policy.api.repository;

import com.policy.api.constants.PaymentFrequency;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ReferenceMasterRepository {

    private static final List<Integer> POLICY_TERMS =
            List.of(10, 15, 20, 25, 30);

    private static final List<PaymentFrequency> PAYMENT_FREQUENCIES =
            List.of(
                    PaymentFrequency.MONTHLY,
                    PaymentFrequency.QUARTERLY,
                    PaymentFrequency.HALF_YEARLY,
                    PaymentFrequency.YEARLY
            );

    public List<Integer> getPolicyTerms() {
        return POLICY_TERMS;
    }

    public List<PaymentFrequency> getPaymentFrequencies() {
        return PAYMENT_FREQUENCIES;
    }
}