package com.logistics.intellistock.core.exception;

import com.logistics.intellistock.dto.response.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(
    ResourceNotFoundException ex,
    WebRequest request) {

    log.error("Resource not found: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.NOT_FOUND.value())
      .error(HttpStatus.NOT_FOUND.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ApiErrorResponse> handleUnauthorizedException(
    UnauthorizedException ex,
    WebRequest request) {

    log.error("Unauthorized access: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.UNAUTHORIZED.value())
      .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiErrorResponse> handleForbiddenException(
    ForbiddenException ex,
    WebRequest request) {

    log.error("Forbidden access: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.FORBIDDEN.value())
      .error(HttpStatus.FORBIDDEN.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ApiErrorResponse> handleBadRequestException(
    BadRequestException ex,
    WebRequest request) {

    log.error("Bad request: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<ApiErrorResponse> handleDuplicateResourceException(
    DuplicateResourceException ex,
    WebRequest request) {

    log.error("Duplicate resource: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.CONFLICT.value())
      .error(HttpStatus.CONFLICT.getReasonPhrase())
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(StockException.class)
  public ResponseEntity<ApiErrorResponse> handleStockException(
    StockException ex,
    WebRequest request) {

    log.error("Stock error: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Stock Error")
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(PredictionException.class)
  public ResponseEntity<ApiErrorResponse> handlePredictionException(
    PredictionException ex,
    WebRequest request) {

    log.error("Prediction error: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .error("Prediction Error")
      .message(ex.getMessage())
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
    MethodArgumentNotValidException ex,
    WebRequest request) {

    log.error("Validation error: {}", ex.getMessage());

    List<String> details = new ArrayList<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      details.add(error.getField() + ": " + error.getDefaultMessage());
    }

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.BAD_REQUEST.value())
      .error("Validation Failed")
      .message("Invalid input data")
      .path(request.getDescription(false).replace("uri=", ""))
      .details(details)
      .build();

    return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
    BadCredentialsException ex,
    WebRequest request) {

    log.error("Bad credentials: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.UNAUTHORIZED.value())
      .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
      .message("Invalid username or password")
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiErrorResponse> handleAuthenticationException(
    AuthenticationException ex,
    WebRequest request) {

    log.error("Authentication error: {}", ex.getMessage());

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.UNAUTHORIZED.value())
      .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
      .message("Authentication failed")
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGlobalException(
    Exception ex,
    WebRequest request) {

    log.error("Unexpected error: {}", ex.getMessage(), ex);

    ApiErrorResponse error = ApiErrorResponse.builder()
      .timestamp(LocalDateTime.now())
      .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
      .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
      .message("An unexpected error occurred")
      .path(request.getDescription(false).replace("uri=", ""))
      .build();

    return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
