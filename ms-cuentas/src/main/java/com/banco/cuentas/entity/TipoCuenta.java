package com.banco.cuentas.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum TipoCuenta {

    AHORROS,
    CORRIENTE;

    /** Acepta "Ahorro", "ahorros", "AHORROS", "Corriente"... sin importar mayúsculas ni singular/plural. */
    @JsonCreator
    public static TipoCuenta from(String valor) {
        if (valor == null) {
            return null;
        }
        return switch (valor.trim().toUpperCase()) {
            case "AHORRO", "AHORROS" -> AHORROS;
            case "CORRIENTE" -> CORRIENTE;
            default -> throw new IllegalArgumentException(
                    "Tipo de cuenta inválido: '" + valor + "'. Valores permitidos: Ahorros, Corriente");
        };
    }
}