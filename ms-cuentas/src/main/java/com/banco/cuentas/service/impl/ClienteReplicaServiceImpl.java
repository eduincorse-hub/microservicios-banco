package com.banco.cuentas.service.impl;

import com.banco.cuentas.entity.ClienteReplica;
import com.banco.cuentas.entity.Cuenta;
import com.banco.cuentas.messaging.ClienteEvent;
import com.banco.cuentas.repository.ClienteReplicaRepository;
import com.banco.cuentas.repository.CuentaRepository;
import com.banco.cuentas.service.ClienteReplicaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClienteReplicaServiceImpl implements ClienteReplicaService {

    private final ClienteReplicaRepository clienteReplicaRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public void sincronizar(ClienteEvent event) {
        switch (event.tipo()) {
            case CREADO, ACTUALIZADO -> guardarOActualizar(event);
            case ELIMINADO -> desactivar(event.clienteId());
        }
    }

    /** Upsert: si existe lo actualiza, si no lo crea. Procesar el mismo evento dos veces da el mismo resultado. */
    private void guardarOActualizar(ClienteEvent event) {
        ClienteReplica replica = clienteReplicaRepository.findById(event.clienteId())
                .orElseGet(() -> new ClienteReplica(event.clienteId(), event.nombre(), event.estado()));

        replica.setNombre(event.nombre());
        replica.setEstado(event.estado());
        clienteReplicaRepository.save(replica);
        log.info("Réplica de cliente sincronizada clienteId={}", event.clienteId());
    }

        /** No se borra: sus cuentas la referencian y el historial debe conservarse. */
    private void desactivar(String clienteId) {
        clienteReplicaRepository.findById(clienteId).ifPresent(replica -> {
            replica.setEstado(Boolean.FALSE);

            List<Cuenta> cuentas = cuentaRepository.findByClienteClienteId(clienteId);
            cuentas.forEach(Cuenta::desactivar);

            log.info("Cliente desactivado clienteId={} y {} cuenta(s) inactivada(s)", clienteId, cuentas.size());
        });
    }
}