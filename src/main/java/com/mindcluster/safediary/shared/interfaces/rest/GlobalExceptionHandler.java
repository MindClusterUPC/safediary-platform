package com.mindcluster.safediary.shared.interfaces.rest;

import com.mindcluster.safediary.shared.application.result.ApplicationError;
import com.mindcluster.safediary.shared.interfaces.rest.transform.ErrorResponseAssembler;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Global exception handler for REST API.
 * Provides centralized exception handling for the entire application,
 * ensuring all unhandled exceptions are translated to consistent
 * HTTP responses via the shared error assembly pattern.
 */
@RestControllerAdvice
@NullMarked
public class GlobalExceptionHandler {
    private static final String MESSAGES_BASENAME = "messages";

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).headers(ex.getHeaders()).body(ex.getBody());
    }

    /**
     * Handles validation exceptions from Spring's request body validation.
     * Maps validation failure to a standardized error response.
     *
     * @param ex the validation exception from @Valid binding
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        var fieldErrors = ex.getBindingResult().getFieldErrors();
        var validationPrefix = resolveMessageOrDefault("validation.field.prefix", "Field");
        var errorDetails = fieldErrors.isEmpty()
                ? resolveMessageOrDefault("validation.request.failed", "Request validation failed")
                : fieldErrors.stream()
                        .map(error -> "%s %s: %s".formatted(
                                validationPrefix,
                                error.getField(),
                                error.getDefaultMessage()
                        ))
                        .reduce((a, b) -> a + "; " + b)
                        .orElse(resolveMessageOrDefault("validation.request.failed", "Request validation failed"));

        var applicationError = ApplicationError.validationError("request-body", errorDetails);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles malformed JSON payloads, invalid enum values, or null primitives during deserialization.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        var message = ex.getMostSpecificCause().getMessage();
        var applicationError = ApplicationError.validationError(
                "request-body",
                message != null && !message.isBlank() ? message : resolveMessageOrDefault("validation.request.failed", "Malformed request body")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid path or query parameter types (e.g. non-numeric IDs).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<?> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        var details = "Invalid value '%s' for parameter '%s'".formatted(ex.getValue(), ex.getName());
        var applicationError = ApplicationError.validationError("request-parameter", details);
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles invalid request arguments such as malformed UUID path or payload values.
     *
     * @param ex the illegal argument exception
     * @return error response with BAD_REQUEST status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        var applicationError = ApplicationError.validationError(
                resolveMessageOrDefault("validation.request.argument", "request-argument"),
                ex.getMessage() != null ? ex.getMessage() : resolveMessageOrDefault("validation.request.failed", "Request validation failed")
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles a request for a resource that belongs to another account.
     *
     * @param ex the access denied exception
     * @return error response with FORBIDDEN status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDeniedException(AccessDeniedException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.forbidden(ex.getMessage()));
    }

    /**
     * Handles a request that needs the authenticated account but carries no valid access token.
     *
     * @param ex the missing credentials exception
     * @return error response with UNAUTHORIZED status
     */
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleMissingCredentials(AuthenticationCredentialsNotFoundException ex) {
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(ApplicationError.unauthorized(ex.getMessage()));
    }

    /**
     * Handles unexpected runtime exceptions not caught by specific handlers.
     * Maps to a generic unexpected error response.
     *
     * @param ex the unhandled runtime exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    /**
     * Handles all other exceptions not matched by specific handlers.
     * Provides a final fallback for any unexpected exception type.
     *
     * @param ex the exception
     * @return error response with INTERNAL_SERVER_ERROR status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception ex) {
        var applicationError = ApplicationError.unexpected(
                resolveMessageOrDefault("error.unexpected.context", "global-exception-handler"),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
        );
        return ErrorResponseAssembler.toErrorResponseFromApplicationError(applicationError);
    }

    private String resolveMessageOrDefault(String key, String defaultValue, Object... args) {
        try {
            var locale = LocaleContextHolder.getLocale();
            var bundle = ResourceBundle.getBundle(MESSAGES_BASENAME, locale);
            if (bundle.containsKey(key)) {
                return MessageFormat.format(bundle.getString(key), args);
            }
        } catch (MissingResourceException ignored) {
            // fallback below
        }
        return defaultValue;
    }
}
