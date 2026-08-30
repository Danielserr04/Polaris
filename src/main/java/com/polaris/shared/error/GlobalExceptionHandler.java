package com.polaris.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Traduce las excepciones de dominio a HTTP. Es el unico sitio de la aplicacion
 * donde se construye una respuesta de error.
 *
 * <p>Los Controllers lanzan y se olvidan. Ver docs/convenciones.md.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), request);
    }

    /**
     * Una API externa ha fallado. 502 y no 500: el problema no esta aqui. Se
     * loguea entero porque el mensaje que llega al cliente es deliberadamente
     * generico.
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ErrorResponse> handleExternalService(ExternalServiceException ex,
                                                               HttpServletRequest request) {
        log.error("Fallo de una API externa en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.BAD_GATEWAY, ex.getMessage(), request);
    }

    /**
     * Fallos de Bean Validation en los DTOs de entrada. Se juntan todos los campos
     * en un solo mensaje para no romper el formato unico de ErrorResponse.
     *
     * <p>BindException y no MethodArgumentNotValidException: la segunda hereda de
     * la primera y solo cubre los @RequestBody. Un DTO validado que llega por
     * query params lanza BindException a secas y se escapaba al 500 generico.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(BindException ex,
                                                              HttpServletRequest request) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /**
     * Red de seguridad. Todo lo que no se ha previsto sale como 500 y se loguea
     * entero: al cliente no se le manda el detalle interno.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String mensaje, HttpServletRequest request) {
        ErrorResponse body = ErrorResponse.of(status.value(), mensaje, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
