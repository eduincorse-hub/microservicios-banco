package com.banco.clientes.service;

import com.banco.clientes.dto.ClientePatchRequest;
import com.banco.clientes.dto.ClienteRequest;
import com.banco.clientes.dto.ClienteResponse;

import java.util.List;

public interface ClienteService {

    ClienteResponse crear(ClienteRequest request);

    List<ClienteResponse> listar();

    ClienteResponse obtener(String clienteId);

    ClienteResponse actualizar(String clienteId, ClienteRequest request);

    ClienteResponse actualizarParcial(String clienteId, ClientePatchRequest request);

    void eliminar(String clienteId);
}