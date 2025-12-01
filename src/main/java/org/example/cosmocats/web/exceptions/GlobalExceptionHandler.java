package org.example.cosmocats.web.exceptions;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.example.cosmocats.featuretoggle.exception.FeatureToggleNotEnabledException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
    List<String> validationErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    String.format("Field '%s': %s", error.getField(), error.getDefaultMessage()))
            .collect(Collectors.toList());

    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed for request parameters");

    problemDetail.setTitle("Validation Exception");
    problemDetail.setType(URI.create("urn:problem-type:validation-error"));
    problemDetail.setProperty("errors", validationErrors);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetail);
  }

  @ExceptionHandler(FeatureToggleNotEnabledException.class)
  public ResponseEntity<Object> handleFeatureToggleNotEnabled(FeatureToggleNotEnabledException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());

    problemDetail.setTitle("Feature Not Available");
    problemDetail.setType(URI.create("urn:problem-type:feature-not-available"));

    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .contentType(MediaType.APPLICATION_PROBLEM_JSON)
        .body(problemDetail);
  }
}
