package com.banco.cuentas.service;

import com.banco.cuentas.messaging.ClienteEvent;

public interface ClienteReplicaService {

    void sincronizar(ClienteEvent event);
}