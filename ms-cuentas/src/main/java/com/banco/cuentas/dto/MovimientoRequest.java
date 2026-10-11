package com.banco.cuentas.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MovimientoRequest(

        @NotNull(message = "El número de cuenta es obligatorio")
        Integer numeroCuenta,

        @NotNull(message = "El valor es obligatorio")
        @Digits(integer = 13, fraction = 2, message = "El valor admite máximo 2 decimales")
        BigDecimal valor
) {
}