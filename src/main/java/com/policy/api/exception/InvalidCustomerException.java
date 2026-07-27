package com.policy.api.exception;

public class InvalidCustomerException extends ApiException {

    public InvalidCustomerException(String message) {
        super(message);
    }
}