package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoCuenta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CuentaRequest(

        @NotNull(message = "El número de cuenta es obligatorio")
        @Positive(message = "El número de cuenta debe ser positivo")
        Integer numeroCuenta,

        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipoCuenta,

        @NotNull(message = "El saldo inicial es obligatorio")
        @PositiveOrZero(message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 13, fraction = 2, message = "El saldo inicial admite máximo 2 decimales")
        BigDecimal saldoInicial,

        Boolean estado,

        @NotBlank(message = "El clienteId es obligatorio")
        String clienteId
) {
}