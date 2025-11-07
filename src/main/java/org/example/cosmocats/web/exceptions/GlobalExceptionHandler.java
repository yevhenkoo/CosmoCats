package org.example.cosmocats.web.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.example.cosmocats.featuretoggle.exception.FeatureToggleNotEnabledException; // <--
                                                                                       // Додайте
                                                                                       // цей імпорт
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorDetails> handleValidationExceptions(
      MethodArgumentNotValidException ex, HttpServletRequest request) {

    FieldError fieldError = ex.getBindingResult().getFieldError();
    String objectName = ex.getBindingResult().getObjectName();

    String errorMessage =
        String.format(
            "Validation failed for object '%s': Field '%s' %s.",
            objectName,
            fieldError != null ? fieldError.getField() : "unknown",
            fieldError != null ? fieldError.getDefaultMessage() : "unknown error");

    ErrorDetails errorDetails =
        new ErrorDetails(
            HttpStatus.BAD_REQUEST.value(), "Bad Request", errorMessage, request.getRequestURI());

    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(FeatureToggleNotEnabledException.class)
  public ResponseEntity<ErrorDetails> handleFeatureToggleNotEnabled(
      FeatureToggleNotEnabledException ex, HttpServletRequest request) {

    ErrorDetails errorDetails =
        new ErrorDetails(
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI());

    return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
  }
}
