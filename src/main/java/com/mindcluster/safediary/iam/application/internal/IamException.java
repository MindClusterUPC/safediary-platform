package com.mindcluster.safediary.iam.application.internal;

import com.mindcluster.safediary.shared.application.result.ApplicationError;
import lombok.Getter;

@Getter
public class IamException extends RuntimeException {
    private final ApplicationError error;
    public IamException(ApplicationError error) { super(error.message()); this.error = error; }
}
