package com.crossover.report.exception;

public enum ValidationReason {
    USERNAME_MISSING("Username is missing"),
    PASSWORD_MISSING("Password is missing"),
    DATE_RANGE_SHOULD_NOT_BE_MORE_THAN_30("Date range should not be more than 30");

    String reason;

    ValidationReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
