package com.banco.cuentas.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cliente_replica")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClienteReplica {

    @Id
    @Column(name = "cliente_id", length = 50)
    private String clienteId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean estado;

    public boolean estaActivo() {
        return Boolean.TRUE.equals(estado);
    }
}