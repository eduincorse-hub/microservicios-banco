package com.banco.cuentas.service.impl;

import com.banco.cuentas.dto.RangoFechas;
import com.banco.cuentas.dto.ReporteCuentaResponse;
import com.banco.cuentas.dto.ReporteMovimientoResponse;
import com.banco.cuentas.dto.ReporteResponse;
import com.banco.cuentas.entity.ClienteReplica;
import com.banco.cuentas.entity.Cuenta;
import com.banco.cuentas.entity.Movimiento;
import com.banco.cuentas.exception.ResourceNotFoundException;
import com.banco.cuentas.repository.ClienteReplicaRepository;
import com.banco.cuentas.repository.CuentaRepository;
import com.banco.cuentas.repository.MovimientoRepository;
import com.banco.cuentas.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final ClienteReplicaRepository clienteReplicaRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    @Override
    public ReporteResponse generarEstadoCuenta(String clienteId, String fecha) {
        RangoFechas rango = RangoFechas.parse(fecha);

        ClienteReplica cliente = clienteReplicaRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un cliente con clienteId '" + clienteId + "'"));

        // Consulta 1: todas las cuentas del cliente (aunque no tengan movimientos en el rango)
        List<Cuenta> cuentas = cuentaRepository.findByClienteClienteId(clienteId);

        // Consulta 2: todos los movimientos del rango, agrupados por número de cuenta
        Map<Integer, List<ReporteMovimientoResponse>> movimientosPorCuenta = movimientoRepository
                .findParaReporte(clienteId, rango.desde(), rango.hastaExclusivo())
                .stream()
                .collect(Collectors.groupingBy(
                        m -> m.getCuenta().getNumeroCuenta(),
                        Collectors.mapping(this::toReporteMovimiento, Collectors.toList())));

        List<ReporteCuentaResponse> detalleCuentas = cuentas.stream()
                .sorted(Comparator.comparing(Cuenta::getNumeroCuenta))
                .map(cuenta -> new ReporteCuentaResponse(
                        cuenta.getNumeroCuenta(),
                        cuenta.getTipoCuenta(),
                        cuenta.getSaldoInicial(),
                        cuenta.getEstado(),
                        cuenta.getSaldoDisponible(),
                        movimientosPorCuenta.getOrDefault(cuenta.getNumeroCuenta(), List.of())))
                .toList();

        return new ReporteResponse(cliente.getClienteId(), cliente.getNombre(),
                rango.inicio(), rango.fin(), detalleCuentas);
    }

    private ReporteMovimientoResponse toReporteMovimiento(Movimiento movimiento) {
        return new ReporteMovimientoResponse(
                movimiento.getFecha(),
                movimiento.getTipoMovimiento(),
                movimiento.getValor(),
                movimiento.getSaldo());
    }
}