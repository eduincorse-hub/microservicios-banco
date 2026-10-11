package com.banco.cuentas.entity;

import java.math.BigDecimal;

public enum TipoMovimiento {

    DEPOSITO,
    RETIRO;

    /** El tipo se deriva del signo del valor: positivo = depósito, negativo = retiro. */
    public static TipoMovimiento desde(BigDecimal valor) {
        return valor.signum() > 0 ? DEPOSITO : RETIRO;
    }
}