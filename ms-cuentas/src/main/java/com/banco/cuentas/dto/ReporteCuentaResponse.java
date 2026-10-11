package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoCuenta;

import java.math.BigDecimal;
import java.util.List;

public record ReporteCuentaResponse(
        Integer numeroCuenta,
        TipoCuenta tipo,
        BigDecimal saldoInicial,
        Boolean estado,
        BigDecimal saldoDisponible,
        List<ReporteMovimientoResponse> movimientos
) {
}