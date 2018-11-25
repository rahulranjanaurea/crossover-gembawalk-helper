package com.crossover.report.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data()
public class AnalyzerException extends RuntimeException {

    private final ExceptionReason reason;

    public AnalyzerException(ExceptionReason reason, Exception e) {
        super(reason.getReason(), e);
        this.reason = reason;
    }
}
