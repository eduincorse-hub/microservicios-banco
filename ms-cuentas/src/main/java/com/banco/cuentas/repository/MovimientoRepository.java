package com.banco.cuentas.repository;

import com.banco.cuentas.entity.Movimiento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MovimientoRepository extends JpaRepository<Movimiento, Integer> {

    @EntityGraph(attributePaths = "cuenta")
    List<Movimiento> findByCuentaNumeroCuentaOrderByFechaAscIdAsc(Integer numeroCuenta);

    @Override
    @EntityGraph(attributePaths = "cuenta")
    List<Movimiento> findAll();

    @Override
    @EntityGraph(attributePaths = "cuenta")
    Optional<Movimiento> findById(Integer id);

    /** Último movimiento de una cuenta (para editar o eliminar solo el último). */
    Optional<Movimiento> findTopByCuentaNumeroCuentaOrderByIdDesc(Integer numeroCuenta);

    boolean existsByCuentaNumeroCuenta(Integer numeroCuenta);

        /** Solo obtiene el número de cuenta de un movimiento, sin cargar la cuenta. */
    @Query("SELECT m.cuenta.numeroCuenta FROM Movimiento m WHERE m.id = :id")
    Optional<Integer> findNumeroCuentaById(@Param("id") Integer id);

        /**
     * Movimientos de todas las cuentas de un cliente en un rango de fechas.
     * Una sola consulta (JOIN FETCH), ordenada por cuenta y fecha.
     */
    @Query("""
            SELECT m FROM Movimiento m
            JOIN FETCH m.cuenta c
            WHERE c.cliente.clienteId = :clienteId
              AND m.fecha >= :desde
              AND m.fecha < :hasta
            ORDER BY c.numeroCuenta, m.fecha, m.id
            """)
    List<Movimiento> findParaReporte(@Param("clienteId") String clienteId,
                                     @Param("desde") LocalDateTime desde,
                                     @Param("hasta") LocalDateTime hasta);
}