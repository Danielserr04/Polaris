package com.polaris.shared.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
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
                .map(error -> error.getField() + ": " + describirFallo(ex, error))
                .collect(Collectors.joining(", "));

        return build(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /**
     * Un query param que no se puede convertir al tipo del DTO (enum, fecha,
     * numero) es un fallo de binding, no de validacion: se traduce a un mensaje
     * legible en vez del texto tecnico de Spring, que trae nombres de clases.
     */
    private String describirFallo(BindException ex, FieldError error) {
        if (!error.isBindingFailure()) {
            return error.getDefaultMessage();
        }
        return "valor no valido" + sufijoValoresAdmitidos(ex.getBindingResult().getFieldType(error.getField()));
    }

    /**
     * Variable de ruta o query param suelto que no se puede convertir, p. ej.
     * /entrada/abc donde va un Long.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            HttpServletRequest request) {
        String mensaje = ex.getName() + ": valor no valido" + sufijoValoresAdmitidos(ex.getRequiredType());
        return build(HttpStatus.BAD_REQUEST, mensaje, request);
    }

    /**
     * Cuerpo JSON que no se puede leer: enum invalido, tipo equivocado, JSON mal
     * formado o cuerpo ausente. Es un error del cliente (400), no nuestro. El
     * mensaje nunca incluye nombres de clases ni el detalle de Jackson.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, mensajeCuerpoIlegible(ex), request);
    }

    private String mensajeCuerpoIlegible(HttpMessageNotReadableException ex) {
        Throwable causa = ex.getCause();
        if (causa instanceof MismatchedInputException mismatch && !mismatch.getPath().isEmpty()) {
            String campo = mismatch.getPath().stream()
                    .map(ref -> ref.getFieldName() != null ? ref.getFieldName() : "[" + ref.getIndex() + "]")
                    .collect(Collectors.joining("."))
                    .replace(".[", "[");
            Class<?> destino = mismatch instanceof InvalidFormatException invalido ? invalido.getTargetType() : null;
            return campo + ": valor no valido" + sufijoValoresAdmitidos(destino);
        }
        if (causa instanceof JsonProcessingException) {
            return "El cuerpo de la peticion no es un JSON valido";
        }
        return "Falta el cuerpo de la peticion";
    }

    /** Si el destino es un enum, lista sus valores; si no, cadena vacia. */
    private String sufijoValoresAdmitidos(Class<?> destino) {
        if (destino == null || !destino.isEnum()) {
            return "";
        }
        String valores = Arrays.stream(destino.getEnumConstants())
                .map(constante -> ((Enum<?>) constante).name())
                .collect(Collectors.joining(", "));
        return ". Valores admitidos: " + valores;
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
