package com.banco.clientes.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@DisplayName("F5 - Pruebas unitarias de la entidad de dominio Cliente")
class ClienteTest {

    @Test
    @DisplayName("Un cliente nuevo nace activo")
    void clienteNuevo_estaActivo() {
        Cliente cliente = new Cliente();

        assertThat(cliente.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("Desactivar y volver a activar un cliente cambia su estado")
    void desactivarYActivar_cambiaEstado() {
        Cliente cliente = new Cliente();

        cliente.desactivar();
        assertThat(cliente.estaActivo()).isFalse();
        assertThat(cliente.getEstado()).isFalse();

        cliente.activar();
        assertThat(cliente.estaActivo()).isTrue();
    }

    @Test
    @DisplayName("Antes de persistir se genera un clienteId UUID válido")
    void prePersist_generaClienteId() {
        Cliente cliente = new Cliente();

        cliente.prePersist();

        assertThat(cliente.getClienteId()).isNotBlank();
        assertThatCode(() -> UUID.fromString(cliente.getClienteId())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("prePersist no sobrescribe un clienteId existente")
    void prePersist_respetaClienteIdExistente() {
        Cliente cliente = new Cliente();
        cliente.setClienteId("cliente-existente");

        cliente.prePersist();

        assertThat(cliente.getClienteId()).isEqualTo("cliente-existente");
    }

    @Test
    @DisplayName("Cliente hereda los datos de Persona")
    void cliente_heredaDePersona() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Jose Lema");
        cliente.setIdentificacion("0501234567");
        cliente.setDireccion("Otavalo sn y principal");

        assertThat(cliente).isInstanceOf(Persona.class);
        assertThat(cliente.getNombre()).isEqualTo("Jose Lema");
        assertThat(cliente.getIdentificacion()).isEqualTo("0501234567");
    }
}