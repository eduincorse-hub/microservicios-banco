package com.banco.cuentas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "movimiento")
@Getter
@Setter
@NoArgsConstructor
public class Movimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "tipo_movimiento", nullable = false, length = 50)
    private TipoMovimiento tipoMovimiento;

    /** Positivo = depósito, negativo = retiro. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    /** Saldo disponible de la cuenta DESPUÉS de aplicar este movimiento. */
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal saldo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "numero_cuenta", nullable = false)
    private Cuenta cuenta;

    /**
     * Solo la Cuenta puede crear movimientos (visibilidad de paquete):
     * así nadie registra un movimiento sin pasar por la validación del saldo.
     */
    static Movimiento registrar(Cuenta cuenta, BigDecimal valor, BigDecimal saldoResultante, LocalDateTime fecha) {
        Movimiento movimiento = new Movimiento();
        movimiento.cuenta = cuenta;
        movimiento.valor = valor;
        movimiento.saldo = saldoResultante;
        movimiento.tipoMovimiento = TipoMovimiento.desde(valor);
        movimiento.fecha = fecha;
        return movimiento;
    }
}