package com.banco.clientes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "persona_id")   // PK de cliente = FK hacia persona.id
@Getter
@Setter
@NoArgsConstructor
public class Cliente extends Persona {

    @Column(name = "cliente_id", nullable = false, unique = true, length = 50, updatable = false)
    private String clienteId;

    @Column(nullable = false, length = 255)
    private String contrasena;

    @Column(nullable = false)
    private Boolean estado = Boolean.TRUE;

    // ---------- Ciclo de vida JPA ----------

    /** Antes de insertar: genera la clave de negocio si no existe. */
    @PrePersist
    void prePersist() {
        if (clienteId == null) {
            clienteId = UUID.randomUUID().toString();
        }
        if (estado == null) {
            estado = Boolean.TRUE;
        }
    }

    // ---------- Comportamiento de dominio ----------

    public void activar() {
        this.estado = Boolean.TRUE;
    }

    public void desactivar() {
        this.estado = Boolean.FALSE;
    }

    public boolean estaActivo() {
        return Boolean.TRUE.equals(estado);
    }
}