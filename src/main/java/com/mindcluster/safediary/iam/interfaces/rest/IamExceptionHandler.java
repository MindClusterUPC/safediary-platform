package com.mindcluster.safediary.iam.interfaces.rest;

import com.mindcluster.safediary.iam.application.internal.IamException;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages="com.mindcluster.safediary.iam.interfaces.rest.controllers")
@Order(0)
public class IamExceptionHandler {
    @ExceptionHandler(IamException.class)
    public ResponseEntity<?> domain(IamException ex) { return ErrorResponseAssembler.toErrorResponseFromApplicationError(ex.getError()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict(DataIntegrityViolationException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.conflict("Iam", "The operation conflicts with an existing record"));
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> malformed(Exception ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.validationError("request", "Invalid identifier, enum, date or JSON payload"));
    }
}
