package com.mindcluster.safediary.carescheduling.application.internal;

import com.mindcluster.safediary.shared.application.result.ApplicationError;
import lombok.Getter;

@Getter
public class CareSchedulingException extends RuntimeException {
    private final ApplicationError error;
    public CareSchedulingException(ApplicationError error) { super(error.message()); this.error = error; }
}
