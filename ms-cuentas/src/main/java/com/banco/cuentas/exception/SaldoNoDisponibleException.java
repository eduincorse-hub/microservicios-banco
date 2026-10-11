package com.banco.cuentas.exception;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SaldoNoDisponibleException extends BusinessRuleException {

    private final Integer numeroCuenta;
    private final BigDecimal saldoDisponible;
    private final BigDecimal valorSolicitado;

    public SaldoNoDisponibleException(Integer numeroCuenta, BigDecimal saldoDisponible, BigDecimal valorSolicitado) {
        super("Saldo no disponible");
        this.numeroCuenta = numeroCuenta;
        this.saldoDisponible = saldoDisponible;
        this.valorSolicitado = valorSolicitado;
    }
}