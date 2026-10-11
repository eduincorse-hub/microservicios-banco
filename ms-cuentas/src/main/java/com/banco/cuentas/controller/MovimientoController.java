package com.banco.cuentas.controller;

import com.banco.cuentas.dto.MovimientoRequest;
import com.banco.cuentas.dto.MovimientoResponse;
import com.banco.cuentas.dto.MovimientoUpdateRequest;
import com.banco.cuentas.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoResponse> registrar(@Valid @RequestBody MovimientoRequest request) {
        MovimientoResponse registrado = movimientoService.registrar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(registrado.id())
                .toUri();
        return ResponseEntity.created(location).body(registrado);
    }

    /** GET /movimientos o GET /movimientos?numeroCuenta=478758 */
    @GetMapping
    public ResponseEntity<List<MovimientoResponse>> listar(@RequestParam(required = false) Integer numeroCuenta) {
        return ResponseEntity.ok(movimientoService.listar(numeroCuenta));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoResponse> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(movimientoService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoResponse> actualizar(@PathVariable Integer id,
                                                         @Valid @RequestBody MovimientoUpdateRequest request) {
        return ResponseEntity.ok(movimientoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        movimientoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}