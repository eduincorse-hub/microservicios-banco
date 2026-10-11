package com.banco.cuentas.entity;

import com.banco.cuentas.exception.BusinessRuleException;
import com.banco.cuentas.exception.SaldoNoDisponibleException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
@NoArgsConstructor
public class Cuenta {

    @Id
    @Column(name = "numero_cuenta")
    private Integer numeroCuenta;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)   // en MySQL se guarda como VARCHAR, igual que en BaseDatos.sql
    @Column(name = "tipo_cuenta", nullable = false, length = 50)
    private TipoCuenta tipoCuenta;

    @Column(name = "saldo_inicial", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoInicial;

    @Column(name = "saldo_disponible", nullable = false, precision = 15, scale = 2)
    private BigDecimal saldoDisponible;

    @Column(nullable = false)
    private Boolean estado = Boolean.TRUE;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClienteReplica cliente;

    // ---------- Creación ----------

    /** Abre una cuenta: el saldo disponible siempre arranca igual al saldo inicial. */
    public static Cuenta abrir(Integer numeroCuenta, TipoCuenta tipoCuenta, BigDecimal saldoInicial,
                               ClienteReplica cliente, Boolean estado) {
        Cuenta cuenta = new Cuenta();
        cuenta.numeroCuenta = numeroCuenta;
        cuenta.tipoCuenta = tipoCuenta;
        cuenta.saldoInicial = saldoInicial;
        cuenta.saldoDisponible = saldoInicial;
        cuenta.cliente = cliente;
        cuenta.estado = estado == null ? Boolean.TRUE : estado;
        return cuenta;
    }

    // ---------- Comportamiento de dominio ----------

    /**
     * F2 y F3: aplica un movimiento y devuelve el registro de la transacción.
     * Valor positivo = depósito; negativo = retiro.
     */
    public Movimiento aplicarMovimiento(BigDecimal valor, LocalDateTime fecha) {
        if (valor == null || valor.signum() == 0) {
            throw new BusinessRuleException("El valor del movimiento debe ser distinto de cero");
        }
        if (!estaActiva()) {
            throw new BusinessRuleException("La cuenta " + numeroCuenta + " está inactiva");
        }

        BigDecimal nuevoSaldo = saldoDisponible.add(valor);
        if (nuevoSaldo.signum() < 0) {
            throw new SaldoNoDisponibleException(numeroCuenta, saldoDisponible, valor);
        }

        this.saldoDisponible = nuevoSaldo;
        return Movimiento.registrar(this, valor, nuevoSaldo, fecha);
    }

    public void activar() {
        this.estado = Boolean.TRUE;
    }

    public void desactivar() {
        this.estado = Boolean.FALSE;
    }

    public boolean estaActiva() {
        return Boolean.TRUE.equals(estado);
    }
}