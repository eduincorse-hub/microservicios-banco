package com.banco.clientes.controller;

import com.banco.clientes.dto.ClientePatchRequest;
import com.banco.clientes.dto.ClienteRequest;
import com.banco.clientes.dto.ClienteResponse;
import com.banco.clientes.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse creado = clienteService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{clienteId}")
                .buildAndExpand(creado.clienteId())
                .toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar() {
        return ResponseEntity.ok(clienteService.listar());
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable String clienteId) {
        return ResponseEntity.ok(clienteService.obtener(clienteId));
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable String clienteId,
                                                      @Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.ok(clienteService.actualizar(clienteId, request));
    }

    @PatchMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> actualizarParcial(@PathVariable String clienteId,
                                                             @Valid @RequestBody ClientePatchRequest request) {
        return ResponseEntity.ok(clienteService.actualizarParcial(clienteId, request));
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> eliminar(@PathVariable String clienteId) {
        clienteService.eliminar(clienteId);
        return ResponseEntity.noContent().build();
    }
}