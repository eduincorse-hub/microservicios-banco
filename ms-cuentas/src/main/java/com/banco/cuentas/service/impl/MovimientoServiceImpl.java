package com.banco.cuentas.service.impl;

import com.banco.cuentas.dto.MovimientoRequest;
import com.banco.cuentas.dto.MovimientoResponse;
import com.banco.cuentas.dto.MovimientoUpdateRequest;
import com.banco.cuentas.entity.Cuenta;
import com.banco.cuentas.entity.Movimiento;
import com.banco.cuentas.exception.BusinessRuleException;
import com.banco.cuentas.exception.ResourceNotFoundException;
import com.banco.cuentas.mapper.MovimientoMapper;
import com.banco.cuentas.repository.CuentaRepository;
import com.banco.cuentas.repository.MovimientoRepository;
import com.banco.cuentas.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoMapper movimientoMapper;
    private final Clock clock;

    /** F2 y F3: registra un depósito (+) o retiro (-) y actualiza el saldo de la cuenta. */
    @Override
    @Transactional
    public MovimientoResponse registrar(MovimientoRequest request) {
        Cuenta cuenta = bloquearCuenta(request.numeroCuenta());

        // La entidad valida: valor != 0, cuenta activa y saldo suficiente (F3)
        Movimiento movimiento = cuenta.aplicarMovimiento(request.valor(), LocalDateTime.now(clock));

        // El saldo de la cuenta se actualiza solo (dirty checking); el movimiento se inserta
        return movimientoMapper.toResponse(movimientoRepository.save(movimiento));
    }

    @Override
    public List<MovimientoResponse> listar(Integer numeroCuenta) {
        List<Movimiento> movimientos = (numeroCuenta == null)
                ? movimientoRepository.findAll()
                : movimientoRepository.findByCuentaNumeroCuentaOrderByFechaAscIdAsc(numeroCuenta);

        return movimientos.stream()
                .map(movimientoMapper::toResponse)
                .toList();
    }

    @Override
    public MovimientoResponse obtener(Integer id) {
        return movimientoMapper.toResponse(buscarMovimiento(id));
    }

    /** Corrige el valor del ÚLTIMO movimiento de la cuenta y recalcula el saldo. */
    @Override
    @Transactional
    public MovimientoResponse actualizar(Integer id, MovimientoUpdateRequest request) {
        Cuenta cuenta = bloquearCuenta(numeroCuentaDe(id));
        Movimiento movimiento = buscarMovimiento(id);
        validarEsUltimoMovimiento(movimiento, cuenta);

        // 1. Se revierte el efecto del movimiento original
        cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().subtract(movimiento.getValor()));

        // 2. Se aplica el nuevo valor con las mismas validaciones de F2/F3
        Movimiento corregido = cuenta.aplicarMovimiento(request.valor(), movimiento.getFecha());

        // 3. Se actualiza el registro existente (no se crea uno nuevo)
        movimiento.setValor(corregido.getValor());
        movimiento.setTipoMovimiento(corregido.getTipoMovimiento());
        movimiento.setSaldo(corregido.getSaldo());

        return movimientoMapper.toResponse(movimiento);
    }

    /** Elimina el ÚLTIMO movimiento de la cuenta y devuelve el saldo a su valor anterior. */
    @Override
    @Transactional
    public void eliminar(Integer id) {
        Cuenta cuenta = bloquearCuenta(numeroCuentaDe(id));
        Movimiento movimiento = buscarMovimiento(id);
        validarEsUltimoMovimiento(movimiento, cuenta);

        cuenta.setSaldoDisponible(cuenta.getSaldoDisponible().subtract(movimiento.getValor()));
        movimientoRepository.delete(movimiento);
    }

    // ---------- Métodos privados ----------

    private Cuenta bloquearCuenta(Integer numeroCuenta) {
        return cuentaRepository.findByIdForUpdate(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la cuenta número " + numeroCuenta));
    }

    private Integer numeroCuentaDe(Integer movimientoId) {
        return movimientoRepository.findNumeroCuentaById(movimientoId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el movimiento con id " + movimientoId));
    }

    private Movimiento buscarMovimiento(Integer id) {
        return movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el movimiento con id " + id));
    }

    /**
     * Solo se puede editar o eliminar el último movimiento: cambiar uno anterior
     * dejaría inconsistentes los saldos de todos los movimientos posteriores.
     */
    private void validarEsUltimoMovimiento(Movimiento movimiento, Cuenta cuenta) {
        Integer ultimoId = movimientoRepository
                .findTopByCuentaNumeroCuentaOrderByIdDesc(cuenta.getNumeroCuenta())
                .map(Movimiento::getId)
                .orElse(null);

        if (!movimiento.getId().equals(ultimoId)) {
            throw new BusinessRuleException("Solo se puede modificar o eliminar el último movimiento de la cuenta "
                    + cuenta.getNumeroCuenta() + ". Para corregir uno anterior, registre un movimiento de ajuste");
        }
    }
}