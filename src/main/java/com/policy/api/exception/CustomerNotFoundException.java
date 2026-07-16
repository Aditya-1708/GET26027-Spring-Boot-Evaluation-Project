package com.policy.api.exception;

public class CustomerNotFoundException extends ApiException {

    public CustomerNotFoundException(String id) {
        super("Customer with id " + id + " not found.");
    }

}
