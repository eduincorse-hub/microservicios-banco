package com.banco.cuentas.repository;

import com.banco.cuentas.entity.ClienteReplica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteReplicaRepository extends JpaRepository<ClienteReplica, String> {
}