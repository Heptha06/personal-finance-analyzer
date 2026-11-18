package com.heptha.finance.exception;

public class DuplicateResourceException extends BusinessRuleViolationException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
