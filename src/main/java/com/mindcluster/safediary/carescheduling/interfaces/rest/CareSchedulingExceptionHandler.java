package com.mindcluster.safediary.carescheduling.interfaces.rest;

import com.mindcluster.safediary.carescheduling.application.internal.CareSchedulingException;
import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages="com.mindcluster.safediary.carescheduling.interfaces.rest.controllers")
@Order(0)
public class CareSchedulingExceptionHandler {
    @ExceptionHandler(CareSchedulingException.class)
    public ResponseEntity<?> domain(CareSchedulingException ex) { return ErrorResponseAssembler.toErrorResponseFromApplicationError(ex.getError()); }
    @ExceptionHandler({DataIntegrityViolationException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<?> conflict(RuntimeException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.conflict("CareScheduling", "The operation conflicts with a concurrent scheduling change; try again"));
    }
    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class})
    public ResponseEntity<?> malformed(Exception ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(
                ApplicationError.validationError("request", "Invalid identifier, date, enum or JSON payload"));
    }
}
