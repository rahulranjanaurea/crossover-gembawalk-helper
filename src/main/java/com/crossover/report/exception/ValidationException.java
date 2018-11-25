package com.crossover.report.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data()
public class ValidationException extends RuntimeException {

    private final ValidationReason reason;

    public ValidationException(ValidationReason reason) {
        super(reason.getReason());
        this.reason = reason;
    }
}
