package com.banco.clientes.service.impl;

import com.banco.clientes.dto.ClientePatchRequest;
import com.banco.clientes.dto.ClienteRequest;
import com.banco.clientes.dto.ClienteResponse;
import com.banco.clientes.entity.Cliente;
import com.banco.clientes.exception.DuplicateResourceException;
import com.banco.clientes.exception.ResourceNotFoundException;
import com.banco.clientes.mapper.ClienteMapper;
import com.banco.clientes.messaging.ClienteEvent;
import com.banco.clientes.messaging.ClienteEvent.TipoEvento;
import com.banco.clientes.repository.ClienteRepository;
import com.banco.clientes.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        validarIdentificacionUnica(request.identificacion());

        Cliente cliente = clienteMapper.toEntity(request);
        cliente.setContrasena(passwordEncoder.encode(request.contrasena()));

        Cliente guardado = clienteRepository.save(cliente);
        publicarEvento(TipoEvento.CREADO, guardado);
        return clienteMapper.toResponse(guardado);
    }

    @Override
    public List<ClienteResponse> listar() {
        return clienteRepository.findAll().stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Override
    public ClienteResponse obtener(String clienteId) {
        return clienteMapper.toResponse(buscarCliente(clienteId));
    }

    @Override
    @Transactional
    public ClienteResponse actualizar(String clienteId, ClienteRequest request) {
        Cliente cliente = buscarCliente(clienteId);

        if (!cliente.getIdentificacion().equals(request.identificacion())) {
            validarIdentificacionUnica(request.identificacion());
        }

        clienteMapper.updateEntity(request, cliente);
        cliente.setContrasena(passwordEncoder.encode(request.contrasena()));

        publicarEvento(TipoEvento.ACTUALIZADO, cliente);
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponse actualizarParcial(String clienteId, ClientePatchRequest request) {
        Cliente cliente = buscarCliente(clienteId);

        if (request.identificacion() != null
                && !cliente.getIdentificacion().equals(request.identificacion())) {
            validarIdentificacionUnica(request.identificacion());
        }

        clienteMapper.patchEntity(request, cliente);
        if (request.contrasena() != null) {
            cliente.setContrasena(passwordEncoder.encode(request.contrasena()));
        }

        publicarEvento(TipoEvento.ACTUALIZADO, cliente);
        return clienteMapper.toResponse(cliente);
    }

    @Override
    @Transactional
    public void eliminar(String clienteId) {
        Cliente cliente = buscarCliente(clienteId);
        clienteRepository.delete(cliente);
        cliente.desactivar();   // el evento informa que el cliente ya no está activo
        publicarEvento(TipoEvento.ELIMINADO, cliente);
    }

    // ---------- Métodos privados ----------

    private Cliente buscarCliente(String clienteId) {
        return clienteRepository.findByClienteId(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un cliente con clienteId '" + clienteId + "'"));
    }

    private void validarIdentificacionUnica(String identificacion) {
        if (clienteRepository.existsByIdentificacion(identificacion)) {
            throw new DuplicateResourceException(
                    "Ya existe un cliente con la identificación '" + identificacion + "'");
        }
    }

    private void publicarEvento(TipoEvento tipo, Cliente cliente) {
        eventPublisher.publishEvent(new ClienteEvent(
                tipo, cliente.getClienteId(), cliente.getNombre(), cliente.estaActivo()));
    }
}