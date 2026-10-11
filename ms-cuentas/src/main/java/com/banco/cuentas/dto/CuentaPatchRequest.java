package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoCuenta;

public record CuentaPatchRequest(
        TipoCuenta tipoCuenta,
        Boolean estado
) {
}