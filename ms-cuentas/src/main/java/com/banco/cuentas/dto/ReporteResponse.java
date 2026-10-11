package com.banco.cuentas.dto;

import java.time.LocalDate;
import java.util.List;

public record ReporteResponse(
        String clienteId,
        String cliente,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        List<ReporteCuentaResponse> cuentas
) {
}