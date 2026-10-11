package com.banco.cuentas.dto;

import com.banco.cuentas.entity.TipoCuenta;
import jakarta.validation.constraints.NotNull;

public record CuentaUpdateRequest(

        @NotNull(message = "El tipo de cuenta es obligatorio")
        TipoCuenta tipoCuenta,

        @NotNull(message = "El estado es obligatorio")
        Boolean estado
) {
}