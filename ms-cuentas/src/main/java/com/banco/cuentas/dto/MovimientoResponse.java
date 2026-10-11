package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimientoResponse(
        Integer id,
        LocalDateTime fecha,
        Integer numeroCuenta,
        TipoMovimiento tipoMovimiento,
        BigDecimal valor,
        BigDecimal saldo
) {
}