package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoMovimiento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReporteMovimientoResponse(
        LocalDateTime fecha,
        TipoMovimiento tipoMovimiento,
        BigDecimal movimiento,
        BigDecimal saldoDisponible
) {
}