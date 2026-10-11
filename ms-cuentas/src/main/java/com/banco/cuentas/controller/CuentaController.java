package com.banco.cuentas.controller;

import com.banco.cuentas.dto.CuentaPatchRequest;
import com.banco.cuentas.dto.CuentaRequest;
import com.banco.cuentas.dto.CuentaResponse;
import com.banco.cuentas.dto.CuentaUpdateRequest;
import com.banco.cuentas.service.CuentaService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaResponse> crear(@Valid @RequestBody CuentaRequest request) {
        CuentaResponse creada = cuentaService.crear(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numeroCuenta}")
                .buildAndExpand(creada.numeroCuenta())
                .toUri();
        return ResponseEntity.created(location).body(creada);
    }

    /** GET /cuentas o GET /cuentas?clienteId=xxx */
    @GetMapping
    public ResponseEntity<List<CuentaResponse>> listar(@RequestParam(required = false) String clienteId) {
        return ResponseEntity.ok(cuentaService.listar(clienteId));
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> obtener(@PathVariable Integer numeroCuenta) {
        return ResponseEntity.ok(cuentaService.obtener(numeroCuenta));
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> actualizar(@PathVariable Integer numeroCuenta,
                                                     @Valid @RequestBody CuentaUpdateRequest request) {
        return ResponseEntity.ok(cuentaService.actualizar(numeroCuenta, request));
    }

    @PatchMapping("/{numeroCuenta}")
    public ResponseEntity<CuentaResponse> actualizarParcial(@PathVariable Integer numeroCuenta,
                                                            @RequestBody CuentaPatchRequest request) {
        return ResponseEntity.ok(cuentaService.actualizarParcial(numeroCuenta, request));
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer numeroCuenta) {
        cuentaService.eliminar(numeroCuenta);
        return ResponseEntity.noContent().build();
    }
}