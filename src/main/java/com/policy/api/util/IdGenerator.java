package com.policy.api.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IdGenerator {

    private final AtomicInteger customerCount = new AtomicInteger(0);
    private final AtomicInteger proposalCount = new AtomicInteger(0);
    private final AtomicInteger auditCount  = new AtomicInteger(0);
    private static final AtomicInteger counter = new AtomicInteger(100000);

    public String generateCustomerId() {
        int count = customerCount.incrementAndGet();
        return String.format("CUS%03d", count);
    }

    public String generateProposalId() {
        int count = proposalCount.incrementAndGet();
        return String.format("PRO%03d", count);
    }

    public String generateAuditId(){
        int count = auditCount.incrementAndGet();
        return String.format("AUD%03d", count);
    }

    public int generatePolicyNumber() {
        return counter.incrementAndGet();
    }

}
