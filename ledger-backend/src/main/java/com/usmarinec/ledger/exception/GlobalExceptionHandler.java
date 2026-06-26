package com.usmarinec.ledger.exception;

import com.usmarinec.ledger.dto.error.ErrorResponse;
import com.usmarinec.ledger.exception.exceptions.LedgerException;
import com.usmarinec.ledger.responses.SuccessFailureResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centralized exception handler for REST controllers.
 *
 * <p>This class converts application, validation, persistence, request parsing, and unexpected
 * exceptions into a consistent {@link SuccessFailureResponse} containing an {@link ErrorResponse}.
 * It also logs important details about each exception before returning the HTTP response.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  /**
   * Handles application-specific ledger exceptions.
   *
   * <p>These exceptions are expected business or domain errors that carry their own HTTP status.
   * Client-side and business-rule errors are logged as warnings, while server-side errors are
   * logged with the full stack trace.
   *
   * @param ex the ledger exception thrown by the application
   * @param request the current HTTP request
   * @return a response entity containing a standardized error response
   */
  @ExceptionHandler(LedgerException.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleLedgerException(
      LedgerException ex, HttpServletRequest request) {
    HttpStatus status = ex.getStatus();

    if (status.is5xxServerError()) {
      log.error(
          "Ledger exception: status={}, path={}, message={}",
          status.value(),
          request.getRequestURI(),
          ex.getMessage(),
          ex);
    } else {
      log.warn(
          "Ledger exception: status={}, path={}, message={}",
          status.value(),
          request.getRequestURI(),
          ex.getMessage());
    }

    return buildErrorResponse(status, ex.getMessage(), request, List.of());
  }

  /**
   * Handles Spring {@link ResponseStatusException} instances.
   *
   * <p>This provides support for existing service/controller code that still throws {@code
   * ResponseStatusException} directly. The exception status and reason are converted into the
   * application's standard error response format.
   *
   * @param ex the response status exception
   * @param request the current HTTP request
   * @return a response entity containing a standardized error response
   */
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleResponseStatusException(
      ResponseStatusException ex, HttpServletRequest request) {
    HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
    String message =
        ex.getReason() == null || ex.getReason().isBlank()
            ? status.getReasonPhrase()
            : ex.getReason();

    log.warn(
        "ResponseStatusException: status={}, path={}, message={}",
        status.value(),
        request.getRequestURI(),
        message);

    return buildErrorResponse(status, message, request, List.of());
  }

  /**
   * Handles request body validation failures.
   *
   * <p>This method is triggered when validation annotations on a {@code @RequestBody} object fail,
   * such as {@code @NotNull}, {@code @NotBlank}, or {@code @Size}. Field-level validation messages
   * are collected and returned in the error details list.
   *
   * @param ex the validation exception containing binding and field errors
   * @param request the current HTTP request
   * @return a bad request response containing validation error details
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleValidationException(
      MethodArgumentNotValidException ex, HttpServletRequest request) {
    List<String> details =
        ex.getBindingResult().getFieldErrors().stream().map(this::formatFieldError).toList();

    log.warn("Validation failed: path={}, errors={}", request.getRequestURI(), details);

    return buildErrorResponse(
        HttpStatus.BAD_REQUEST, "Request validation failed", request, details);
  }

  /**
   * Handles constraint violations from method parameter or path/query parameter validation.
   *
   * <p>This method is typically used for validation failures outside the request body, such as
   * invalid request parameters or path variables. Constraint violation messages are collected and
   * returned in the error details list.
   *
   * @param ex the constraint violation exception
   * @param request the current HTTP request
   * @return a bad request response containing constraint violation details
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {
    List<String> details =
        ex.getConstraintViolations().stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .toList();

    log.warn("Constraint validation failed: path={}, errors={}", request.getRequestURI(), details);

    return buildErrorResponse(
        HttpStatus.BAD_REQUEST, "Request validation failed", request, details);
  }

  /**
   * Handles malformed or unreadable HTTP request bodies.
   *
   * <p>This method is triggered when Spring cannot deserialize the request body, such as when the
   * JSON is malformed, a field has an invalid enum value, or the request body does not match the
   * expected DTO structure.
   *
   * @param ex the message conversion exception
   * @param request the current HTTP request
   * @return a bad request response indicating that the request body is malformed
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleUnreadableMessage(
      HttpMessageNotReadableException ex, HttpServletRequest request) {
    log.warn(
        "Malformed request body: path={}, message={}", request.getRequestURI(), ex.getMessage());

    return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request, List.of());
  }

  /**
   * Handles database constraint and integrity violations.
   *
   * <p>This method is triggered when a database operation violates a constraint, such as a unique
   * key, foreign key, not-null constraint, or check constraint. The detailed database error is
   * logged, but the API response returns a safe, general conflict message.
   *
   * @param ex the data integrity violation exception
   * @param request the current HTTP request
   * @return a conflict response indicating that the request violates existing data constraints
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleDataIntegrityViolation(
      DataIntegrityViolationException ex, HttpServletRequest request) {
    log.warn(
        "Data integrity violation: path={}, message={}",
        request.getRequestURI(),
        ex.getMostSpecificCause().getMessage());

    return buildErrorResponse(
        HttpStatus.CONFLICT,
        "Request conflicts with existing data or violates a database constraint",
        request,
        List.of());
  }

  /**
   * Handles all otherwise unhandled exceptions.
   *
   * <p>This is the fallback exception handler for unexpected server-side failures. The full
   * exception is logged with a stack trace, while the client receives a generic internal server
   * error response to avoid leaking implementation details.
   *
   * @param ex the unexpected exception
   * @param request the current HTTP request
   * @return an internal server error response with a generic error message
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<SuccessFailureResponse<ErrorResponse>> handleUnexpectedException(
      Exception ex, HttpServletRequest request) {
    log.error(
        "Unexpected exception: path={}, message={}", request.getRequestURI(), ex.getMessage(), ex);

    return buildErrorResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected server error occurred",
        request,
        List.of());
  }

  private String formatFieldError(FieldError fieldError) {
    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
  }

  private ResponseEntity<SuccessFailureResponse<ErrorResponse>> buildErrorResponse(
      HttpStatus status, String message, HttpServletRequest request, List<String> details) {
    ErrorResponse error =
        new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            message,
            request.getRequestURI(),
            details);

    SuccessFailureResponse<ErrorResponse> body =
        SuccessFailureResponse.<ErrorResponse>builder()
            .success(false)
            .message(message)
            .httpStatus(status.getReasonPhrase())
            .items(List.of(error))
            .build();

    return new ResponseEntity<>(body, status);
  }
}
