package com.mindcluster.safediary.cliniciandirectory.interfaces.rest;

import com.mindcluster.safediary.cliniciandirectory.application.internal.DirectoryException;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice(basePackages="com.mindcluster.safediary.cliniciandirectory.interfaces.rest.controllers")
@Order(0)
public class DirectoryExceptionHandler {
    @ExceptionHandler(DirectoryException.class)
    public ResponseEntity<?> domain(DirectoryException ex) { return ErrorResponseAssembler.toErrorResponseFromApplicationError(ex.getError()); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict(DataIntegrityViolationException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.conflict("ClinicianDirectory", "The operation conflicts with an existing directory record"));
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<?> malformed(Exception ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.validationError("request", "Invalid identifier, enum or JSON payload"));
    }
}
