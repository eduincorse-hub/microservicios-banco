package com.banco.cuentas.controller;

import com.banco.cuentas.dto.ReporteResponse;
import com.banco.cuentas.service.ReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    /** GET /reportes?fecha=2026-10-01,2026-10-11&cliente={clienteId} */
    @GetMapping
    public ResponseEntity<ReporteResponse> estadoDeCuenta(@RequestParam String fecha,
                                                          @RequestParam String cliente) {
        return ResponseEntity.ok(reporteService.generarEstadoCuenta(cliente, fecha));
    }
}