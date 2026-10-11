package com.banco.cuentas.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ---------- Errores de negocio ----------

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return build(HttpStatus.CONFLICT, "Recurso duplicado", ex.getMessage());
    }

        /** F3: retiro mayor al saldo disponible. */
    @ExceptionHandler(SaldoNoDisponibleException.class)
    public ProblemDetail handleSaldoNoDisponible(SaldoNoDisponibleException ex) {
        ProblemDetail problem = build(HttpStatus.UNPROCESSABLE_CONTENT, "Saldo no disponible", ex.getMessage());
        problem.setProperty("numeroCuenta", ex.getNumeroCuenta());
        problem.setProperty("saldoDisponible", ex.getSaldoDisponible());
        problem.setProperty("valorSolicitado", ex.getValorSolicitado());
        return problem;
    }

        @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_CONTENT, "Regla de negocio no cumplida", ex.getMessage());
    }

    // ---------- Errores de la petición ----------

    /** Falla de Bean Validation (@NotBlank, @Size, @Pattern...). Devuelve el detalle por campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errores.putIfAbsent(error.getField(), error.getDefaultMessage()));

        ProblemDetail problem = build(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "Uno o más campos no son válidos");
        problem.setProperty("errores", errores);
        return problem;
    }

    /** JSON mal formado o con tipos incorrectos (ej. "edad": "abc"). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleNotReadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Petición mal formada",
                "El cuerpo de la petición no es un JSON válido o tiene tipos incorrectos");
    }

    /** Parámetro de URL con tipo incorrecto. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST, "Parámetro inválido",
                "El valor '" + ex.getValue() + "' no es válido para el parámetro '" + ex.getName() + "'");
    }

    // ---------- Red de seguridad de la base de datos ----------

    /** Ej.: dos peticiones simultáneas con la misma identificación (la BD rechaza la segunda). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación viola una restricción de datos (registro duplicado o en uso)");
    }

    // ---------- Cualquier otro error ----------

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        // Excepciones propias de Spring (404 de ruta inexistente, 405 método no permitido, etc.)
        // ya traen su código HTTP correcto: las respetamos.
        if (ex instanceof ErrorResponse errorResponse) {
            ProblemDetail problem = errorResponse.getBody();
            problem.setProperty("timestamp", Instant.now());
            return problem;
        }

        log.error("Error no controlado", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno",
                "Ocurrió un error inesperado. Intente nuevamente más tarde");
    }

    // ---------- Utilidad ----------

    private ProblemDetail build(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}