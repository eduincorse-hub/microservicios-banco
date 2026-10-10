package com.banco.clientes.repository;

import com.banco.clientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByClienteId(String clienteId);

    boolean existsByIdentificacion(String identificacion);
}