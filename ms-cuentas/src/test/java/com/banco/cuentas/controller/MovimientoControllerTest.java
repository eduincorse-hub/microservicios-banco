package com.banco.cuentas.controller;

import com.banco.cuentas.dto.MovimientoRequest;
import com.banco.cuentas.dto.MovimientoResponse;
import com.banco.cuentas.entity.TipoMovimiento;
import com.banco.cuentas.exception.SaldoNoDisponibleException;
import com.banco.cuentas.service.MovimientoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovimientoController.class)
@DisplayName("Pruebas unitarias del endpoint /movimientos")
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovimientoService movimientoService;

    @Test
    @DisplayName("POST /movimientos válido responde 201 con el saldo actualizado")
    void registrarMovimiento_valido_responde201() throws Exception {
        MovimientoResponse respuesta = new MovimientoResponse(1, LocalDateTime.of(2026, 10, 11, 10, 0),
                478758, TipoMovimiento.RETIRO, new BigDecimal("-575.00"), new BigDecimal("1425.00"));
        given(movimientoService.registrar(any(MovimientoRequest.class))).willReturn(respuesta);

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "numeroCuenta": 478758, "valor": -575 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("RETIRO"))
                .andExpect(jsonPath("$.saldo").value(1425.0));
    }

    @Test
    @DisplayName("F3: POST /movimientos sin saldo responde 422 'Saldo no disponible'")
    void registrarMovimiento_sinSaldo_responde422() throws Exception {
        given(movimientoService.registrar(any(MovimientoRequest.class)))
                .willThrow(new SaldoNoDisponibleException(495878, new BigDecimal("150.00"), new BigDecimal("-200")));

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "numeroCuenta": 495878, "valor": -200 }
                                """))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.title").value("Saldo no disponible"))
                .andExpect(jsonPath("$.detail").value("Saldo no disponible"))
                .andExpect(jsonPath("$.numeroCuenta").value(495878))
                .andExpect(jsonPath("$.saldoDisponible").value(150.0));
    }

    @Test
    @DisplayName("POST /movimientos sin datos responde 400 sin llegar al servicio")
    void registrarMovimiento_sinDatos_responde400() throws Exception {
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.numeroCuenta").exists())
                .andExpect(jsonPath("$.errores.valor").exists());

        then(movimientoService).shouldHaveNoInteractions();
    }
}