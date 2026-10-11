package com.banco.cuentas.repository;

import com.banco.cuentas.entity.Cuenta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;



public interface CuentaRepository extends JpaRepository<Cuenta, Integer> {

    /** Carga la cuenta junto con su cliente en una sola consulta (evita el problema N+1). */
    @Override
    @EntityGraph(attributePaths = "cliente")
    List<Cuenta> findAll();

    @Override
    @EntityGraph(attributePaths = "cliente")
    Optional<Cuenta> findById(Integer numeroCuenta);

    @EntityGraph(attributePaths = "cliente")
    List<Cuenta> findByClienteClienteId(String clienteId);

        /**
     * Bloquea la fila de la cuenta hasta que termine la transacción (SELECT ... FOR UPDATE).
     * Evita que dos movimientos simultáneos lean el mismo saldo.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cuenta c WHERE c.numeroCuenta = :numeroCuenta")
    Optional<Cuenta> findByIdForUpdate(@Param("numeroCuenta") Integer numeroCuenta);
}