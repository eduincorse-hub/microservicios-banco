package com.banco.cuentas.service;

import com.banco.cuentas.dto.ReporteResponse;

public interface ReporteService {

    ReporteResponse generarEstadoCuenta(String clienteId, String fecha);
}