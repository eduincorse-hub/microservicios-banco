package com.banco.cuentas.service.impl;

import com.banco.cuentas.dto.CuentaPatchRequest;
import com.banco.cuentas.dto.CuentaRequest;
import com.banco.cuentas.dto.CuentaResponse;
import com.banco.cuentas.dto.CuentaUpdateRequest;
import com.banco.cuentas.entity.ClienteReplica;
import com.banco.cuentas.entity.Cuenta;
import com.banco.cuentas.exception.BusinessRuleException;
import com.banco.cuentas.exception.DuplicateResourceException;
import com.banco.cuentas.exception.ResourceNotFoundException;
import com.banco.cuentas.mapper.CuentaMapper;
import com.banco.cuentas.repository.ClienteReplicaRepository;
import com.banco.cuentas.repository.CuentaRepository;
import com.banco.cuentas.repository.MovimientoRepository;
import com.banco.cuentas.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final ClienteReplicaRepository clienteReplicaRepository;
    private final MovimientoRepository movimientoRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    @Transactional
    public CuentaResponse crear(CuentaRequest request) {
        if (cuentaRepository.existsById(request.numeroCuenta())) {
            throw new DuplicateResourceException(
                    "Ya existe una cuenta con el número " + request.numeroCuenta());
        }

        ClienteReplica cliente = clienteReplicaRepository.findById(request.clienteId())
                .orElseThrow(() -> new BusinessRuleException(
                        "El cliente '" + request.clienteId() + "' no existe o aún no ha sido sincronizado"));

        if (!cliente.estaActivo()) {
            throw new BusinessRuleException(
                    "El cliente '" + cliente.getNombre() + "' está inactivo; no se le pueden abrir cuentas");
        }

        Cuenta cuenta = Cuenta.abrir(request.numeroCuenta(), request.tipoCuenta(),
                request.saldoInicial(), cliente, request.estado());

        return cuentaMapper.toResponse(cuentaRepository.save(cuenta));
    }

    @Override
    public List<CuentaResponse> listar(String clienteId) {
        List<Cuenta> cuentas = (clienteId == null || clienteId.isBlank())
                ? cuentaRepository.findAll()
                : cuentaRepository.findByClienteClienteId(clienteId);

        return cuentas.stream()
                .map(cuentaMapper::toResponse)
                .toList();
    }

    @Override
    public CuentaResponse obtener(Integer numeroCuenta) {
        return cuentaMapper.toResponse(buscarCuenta(numeroCuenta));
    }

    @Override
    @Transactional
    public CuentaResponse actualizar(Integer numeroCuenta, CuentaUpdateRequest request) {
        Cuenta cuenta = buscarCuenta(numeroCuenta);
        cuentaMapper.updateEntity(request, cuenta);
        validarActivacion(cuenta);
        return cuentaMapper.toResponse(cuenta);
    }

    @Override
    @Transactional
    public CuentaResponse actualizarParcial(Integer numeroCuenta, CuentaPatchRequest request) {
        Cuenta cuenta = buscarCuenta(numeroCuenta);
        cuentaMapper.patchEntity(request, cuenta);
        validarActivacion(cuenta);
        return cuentaMapper.toResponse(cuenta);
    }

    @Override
    @Transactional
    public void eliminar(Integer numeroCuenta) {
        Cuenta cuenta = buscarCuenta(numeroCuenta);

        if (movimientoRepository.existsByCuentaNumeroCuenta(numeroCuenta)) {
            throw new BusinessRuleException("La cuenta " + numeroCuenta
                    + " tiene movimientos registrados y no puede eliminarse; desactívela con PATCH");
        }
        cuentaRepository.delete(cuenta);
    }

    // ---------- Métodos privados ----------

    private Cuenta buscarCuenta(Integer numeroCuenta) {
        return cuentaRepository.findById(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la cuenta número " + numeroCuenta));
    }

    /** No se puede reactivar una cuenta si su cliente está inactivo. */
    private void validarActivacion(Cuenta cuenta) {
        if (cuenta.estaActiva() && !cuenta.getCliente().estaActivo()) {
            throw new BusinessRuleException(
                    "No se puede activar la cuenta " + cuenta.getNumeroCuenta() + ": el cliente está inactivo");
        }
    }
}