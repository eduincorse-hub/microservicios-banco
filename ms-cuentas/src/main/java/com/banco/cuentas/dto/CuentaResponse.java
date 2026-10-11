package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoCuenta;

import java.math.BigDecimal;

public record CuentaResponse(
        Integer numeroCuenta,
        TipoCuenta tipoCuenta,
        BigDecimal saldoInicial,
        BigDecimal saldoDisponible,
        Boolean estado,
        String clienteId,
        String cliente
) {
}