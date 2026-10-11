package com.banco.clientes.controller;

import com.banco.clientes.dto.ClienteRequest;
import com.banco.clientes.dto.ClienteResponse;
import com.banco.clientes.exception.ResourceNotFoundException;
import com.banco.clientes.service.ClienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@DisplayName("Pruebas unitarias del endpoint /clientes")
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    @DisplayName("POST /clientes con datos válidos responde 201, Location y sin contraseña")
    void crearCliente_datosValidos_responde201() throws Exception {
        ClienteResponse respuesta = new ClienteResponse("abc-123", "Jose Lema", "Masculino", 30,
                "0501234567", "Otavalo sn y principal", "098254785", true);
        given(clienteService.crear(any(ClienteRequest.class))).willReturn(respuesta);

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Jose Lema",
                                  "genero": "Masculino",
                                  "edad": 30,
                                  "identificacion": "0501234567",
                                  "direccion": "Otavalo sn y principal",
                                  "telefono": "098254785",
                                  "contrasena": "1234",
                                  "estado": true
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/clientes/abc-123")))
                .andExpect(jsonPath("$.clienteId").value("abc-123"))
                .andExpect(jsonPath("$.nombre").value("Jose Lema"))
                .andExpect(jsonPath("$.contrasena").doesNotExist());
    }

    @Test
    @DisplayName("POST /clientes con datos inválidos responde 400 con el detalle por campo")
    void crearCliente_datosInvalidos_responde400() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "telefono": "abc"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Datos inválidos"))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.identificacion").exists())
                .andExpect(jsonPath("$.errores.contrasena").exists())
                .andExpect(jsonPath("$.errores.telefono").exists());

        then(clienteService).shouldHaveNoInteractions();   // la validación corta antes del servicio
    }

    @Test
    @DisplayName("GET /clientes/{id} inexistente responde 404 con ProblemDetail")
    void obtenerCliente_inexistente_responde404() throws Exception {
        given(clienteService.obtener("no-existe"))
                .willThrow(new ResourceNotFoundException("No existe un cliente con clienteId 'no-existe'"));

        mockMvc.perform(get("/clientes/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No existe un cliente con clienteId 'no-existe'"));
    }
}