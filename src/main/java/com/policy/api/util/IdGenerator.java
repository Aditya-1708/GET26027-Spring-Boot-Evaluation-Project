package com.policy.api.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class IdGenerator {

    private final AtomicInteger customerCount = new AtomicInteger(0);

    public String generateCustomerID() {
        int count = customerCount.incrementAndGet();
        System.out.println("Customer Count = " + count);
        return String.format("CUS%03d", count);
    }
    
}
