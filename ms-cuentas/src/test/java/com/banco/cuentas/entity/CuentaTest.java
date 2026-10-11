package com.banco.cuentas.entity;

import com.banco.cuentas.exception.BusinessRuleException;
import com.banco.cuentas.exception.SaldoNoDisponibleException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pruebas unitarias de la entidad Cuenta (F2 y F3)")
class CuentaTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 11, 10, 0);

    private Cuenta nuevaCuenta(String saldoInicial) {
        ClienteReplica cliente = new ClienteReplica("cli-001", "Jose Lema", true);
        return Cuenta.abrir(478758, TipoCuenta.AHORROS, new BigDecimal(saldoInicial), cliente, true);
    }

    @Test
    @DisplayName("Al abrir una cuenta, el saldo disponible es igual al saldo inicial")
    void abrir_saldoDisponibleIgualAlInicial() {
        Cuenta cuenta = nuevaCuenta("2000");

        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("2000");
        assertThat(cuenta.estaActiva()).isTrue();
    }

    @Test
    @DisplayName("F2: un depósito suma al saldo y queda registrado como DEPOSITO")
    void deposito_actualizaSaldo() {
        Cuenta cuenta = nuevaCuenta("100");

        Movimiento movimiento = cuenta.aplicarMovimiento(new BigDecimal("600"), AHORA);

        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("700");
        assertThat(movimiento.getTipoMovimiento()).isEqualTo(TipoMovimiento.DEPOSITO);
        assertThat(movimiento.getSaldo()).isEqualByComparingTo("700");
        assertThat(movimiento.getFecha()).isEqualTo(AHORA);
    }

    @Test
    @DisplayName("F2: un retiro por el saldo exacto deja la cuenta en 0 (caso 496825)")
    void retiroExacto_dejaSaldoEnCero() {
        Cuenta cuenta = nuevaCuenta("540");

        Movimiento movimiento = cuenta.aplicarMovimiento(new BigDecimal("-540"), AHORA);

        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("0");
        assertThat(movimiento.getTipoMovimiento()).isEqualTo(TipoMovimiento.RETIRO);
    }

    @Test
    @DisplayName("F3: un retiro mayor al saldo lanza 'Saldo no disponible' y no altera el saldo")
    void retiroSinSaldo_lanzaSaldoNoDisponible() {
        Cuenta cuenta = nuevaCuenta("150");

        assertThatThrownBy(() -> cuenta.aplicarMovimiento(new BigDecimal("-200"), AHORA))
                .isInstanceOf(SaldoNoDisponibleException.class)
                .hasMessage("Saldo no disponible");

        assertThat(cuenta.getSaldoDisponible()).isEqualByComparingTo("150");
    }

    @Test
    @DisplayName("No se permiten movimientos en una cuenta inactiva")
    void cuentaInactiva_rechazaMovimientos() {
        Cuenta cuenta = nuevaCuenta("1000");
        cuenta.desactivar();

        assertThatThrownBy(() -> cuenta.aplicarMovimiento(new BigDecimal("100"), AHORA))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("inactiva");
    }

    @Test
    @DisplayName("No se permiten movimientos con valor cero")
    void valorCero_esRechazado() {
        Cuenta cuenta = nuevaCuenta("1000");

        assertThatThrownBy(() -> cuenta.aplicarMovimiento(BigDecimal.ZERO, AHORA))
                .isInstanceOf(BusinessRuleException.class);
    }
}