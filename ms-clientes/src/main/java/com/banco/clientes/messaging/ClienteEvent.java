package com.banco.clientes.messaging;

public record ClienteEvent(
        TipoEvento tipo,
        String clienteId,
        String nombre,
        Boolean estado
) {
    public enum TipoEvento {
        CREADO,
        ACTUALIZADO,
        ELIMINADO
    }
}