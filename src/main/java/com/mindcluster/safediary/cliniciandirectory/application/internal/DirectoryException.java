package com.mindcluster.safediary.cliniciandirectory.application.internal;

import com.mindcluster.safediary.shared.application.result.ApplicationError;
import lombok.Getter;
@Getter
public class DirectoryException extends RuntimeException {
    private final ApplicationError error;
    public DirectoryException(ApplicationError error) { super(error.message()); this.error = error; }
}
