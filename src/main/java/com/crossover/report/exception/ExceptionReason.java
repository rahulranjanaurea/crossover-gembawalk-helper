package com.crossover.report.exception;

public enum ExceptionReason {
   ;
    String reason;

    private ExceptionReason(String reason) {
        this.reason = reason;
    }

    public String getReason() {
        return reason;
    }
}
