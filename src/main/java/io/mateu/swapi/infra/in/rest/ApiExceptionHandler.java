package io.mateu.swapi.infra.in.rest;

import io.mateu.swapi.infra.in.rest.query.TooManyValuesException;
import io.mateu.swapi.infra.in.rest.query.UnknownFilterException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * One error shape for the whole API. A caller integrating against this should never have to tell a
 * Spring stack trace from an answer, so every failure comes back as the same small JSON object.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<Map<String, Object>> notFound(NotFoundException e) {
    return body(HttpStatus.NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler({
    BadReferenceException.class, UnknownFilterException.class, TooManyValuesException.class
  })
  public ResponseEntity<Map<String, Object>> badRequest(RuntimeException e) {
    return body(HttpStatus.BAD_REQUEST, e.getMessage());
  }

  @ExceptionHandler(CollectionFullException.class)
  public ResponseEntity<Map<String, Object>> full(CollectionFullException e) {
    return body(HttpStatus.INSUFFICIENT_STORAGE, e.getMessage());
  }

  /** A `?height_from=tall` is the caller's mistake, not a server fault — 400, not 500. */
  @ExceptionHandler({NumberFormatException.class, IllegalArgumentException.class})
  public ResponseEntity<Map<String, Object>> unparseable(RuntimeException e) {
    return body(HttpStatus.BAD_REQUEST, "Unparseable filter value: " + e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
    String detail = e.getBindingResult().getFieldErrors().stream()
        .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Invalid body");
    return body(HttpStatus.BAD_REQUEST, detail);
  }

  /**
   * A constraint the DTO does not declare but the entity does fires at flush time, well past the
   * controller. It is still the caller's bad body, so it is still a 400 — this is the net under the
   * @Valid above, not a substitute for it.
   */
  @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> violated(jakarta.validation.ConstraintViolationException e) {
    String detail = e.getConstraintViolations().stream()
        .map(v -> v.getPropertyPath() + " " + v.getMessage())
        .reduce((a, b) -> a + "; " + b)
        .orElse("Invalid body");
    return body(HttpStatus.BAD_REQUEST, detail);
  }

  private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("timestamp", Instant.now().toString());
    payload.put("status", status.value());
    payload.put("error", status.getReasonPhrase());
    payload.put("message", message);
    return ResponseEntity.status(status).body(payload);
  }
}
