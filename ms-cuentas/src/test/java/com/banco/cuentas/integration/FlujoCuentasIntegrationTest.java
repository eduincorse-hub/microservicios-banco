package com.banco.cuentas.integration;

import com.banco.cuentas.config.RabbitMQConfig;
import com.banco.cuentas.messaging.ClienteEvent;
import com.banco.cuentas.messaging.ClienteEvent.TipoEvento;
import com.banco.cuentas.repository.ClienteReplicaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;
import org.testcontainers.utility.MountableFile;

import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * F6 - Prueba de integración: levanta MySQL y RabbitMQ REALES en contenedores
 * y recorre el flujo completo del microservicio de cuentas.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@DisplayName("F6 - Integración: evento de cliente → cuenta → movimientos → reporte")
class FlujoCuentasIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.0")
            .withDatabaseName("db_banco_cuentas")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("../BaseDatos.sql"),
                    "/docker-entrypoint-initdb.d/BaseDatos.sql");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4-management");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ClienteReplicaRepository clienteReplicaRepository;

    @Test
    @DisplayName("Flujo completo con comunicación asíncrona, F2, F3 y F4")
    void flujoCompleto() throws Exception {
        String clienteId = "cliente-integracion-001";

        // 1. Comunicación asíncrona: llega el evento ClienteCreado por RabbitMQ
        rabbitTemplate.convertAndSend(RabbitMQConfig.CLIENTES_EXCHANGE, "cliente.creado",
                new ClienteEvent(TipoEvento.CREADO, clienteId, "Jose Lema", true));
        esperarReplicaSincronizada(clienteId);

        // 2. Crear cuenta (caso de uso 2)
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "numeroCuenta": 478758,
                                  "tipoCuenta": "Ahorro",
                                  "saldoInicial": 2000,
                                  "estado": true,
                                  "clienteId": "%s"
                                }
                                """.formatted(clienteId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoCuenta").value("AHORROS"))
                .andExpect(jsonPath("$.cliente").value("Jose Lema"));

        // 3. F2: retiro de 575 (caso de uso 4) → saldo 1425
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "numeroCuenta": 478758, "valor": -575 }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("RETIRO"))
                .andExpect(jsonPath("$.saldo").value(1425.0));

        // 4. F3: retiro mayor al saldo → 422 "Saldo no disponible"
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "numeroCuenta": 478758, "valor": -5000 }
                                """))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.detail").value("Saldo no disponible"));

        // 5. El saldo persistido en MySQL no cambió con el retiro rechazado
        mockMvc.perform(get("/cuentas/478758"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldoDisponible").value(1425.0));

        // 6. F4: reporte de estado de cuenta
        LocalDate hoy = LocalDate.now(ZoneId.of("America/Guayaquil"));
        String rango = hoy.minusDays(1) + "," + hoy.plusDays(1);

        mockMvc.perform(get("/reportes").param("fecha", rango).param("cliente", clienteId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Jose Lema"))
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value(478758))
                .andExpect(jsonPath("$.cuentas[0].saldoDisponible").value(1425.0))
                .andExpect(jsonPath("$.cuentas[0].movimientos.length()").value(1))
                .andExpect(jsonPath("$.cuentas[0].movimientos[0].movimiento").value(-575.0));
    }

    /** El consumo es asíncrono: se espera (máx. 10 s) a que el listener guarde la réplica. */
    private void esperarReplicaSincronizada(String clienteId) throws InterruptedException {
        for (int intento = 0; intento < 50; intento++) {
            if (clienteReplicaRepository.existsById(clienteId)) {
                return;
            }
            Thread.sleep(200);
        }
        fail("La réplica del cliente no se sincronizó vía RabbitMQ en 10 segundos");
    }
}