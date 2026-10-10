-- =========================================================================
-- SCRIPT DE BASE DE DATOS - RETO TÉCNICO
-- Motor: MySQL 8.0
-- =========================================================================

CREATE DATABASE IF NOT EXISTS db_banco_clientes;
CREATE DATABASE IF NOT EXISTS db_banco_cuentas;

-- -------------------------------------------------------------------------
-- 1. MICROSERVICIO CLIENTES (ms-clientes)
-- -------------------------------------------------------------------------
USE db_banco_clientes;

CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    genero VARCHAR(20),
    edad INT,
    identificacion VARCHAR(20) UNIQUE NOT NULL,
    direccion VARCHAR(255),
    telefono VARCHAR(20)
);

-- [AJUSTE 1] La PK es persona_id (herencia JPA JOINED).
--            cliente_id se mantiene como clave única.
CREATE TABLE cliente (
    persona_id INT PRIMARY KEY,
    cliente_id VARCHAR(50) UNIQUE NOT NULL,
    contrasena VARCHAR(255) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_persona_cliente FOREIGN KEY (persona_id) REFERENCES persona(id) ON DELETE CASCADE
);


-- -------------------------------------------------------------------------
-- 2. MICROSERVICIO CUENTAS (ms-cuentas)
-- -------------------------------------------------------------------------
USE db_banco_cuentas;

-- [AJUSTE 4] Copia local del cliente, alimentada por eventos de RabbitMQ
CREATE TABLE cliente_replica (
    cliente_id VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    estado BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cuenta (
    numero_cuenta INT PRIMARY KEY,
    tipo_cuenta VARCHAR(50) NOT NULL,
    saldo_inicial DECIMAL(15,2) NOT NULL,
    saldo_disponible DECIMAL(15,2) NOT NULL,   -- [AJUSTE 2] Saldo actual de la cuenta
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    cliente_id VARCHAR(50) NOT NULL,           -- Referencia lógica al cliente
    CONSTRAINT fk_cliente_cuenta FOREIGN KEY (cliente_id) REFERENCES cliente_replica(cliente_id)
);

CREATE TABLE movimiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha DATETIME NOT NULL,
    tipo_movimiento VARCHAR(50) NOT NULL,
    valor DECIMAL(15,2) NOT NULL,         -- Positivo (depósito) o negativo (retiro)
    saldo DECIMAL(15,2) NOT NULL,         -- Saldo disponible resultante
    numero_cuenta INT NOT NULL,
    -- [AJUSTE 3] RESTRICT: no se puede borrar una cuenta con historial
    CONSTRAINT fk_cuenta_movimiento FOREIGN KEY (numero_cuenta) REFERENCES cuenta(numero_cuenta) ON DELETE RESTRICT
);

-- Índice para el reporte: movimientos de una cuenta en un rango de fechas
CREATE INDEX idx_movimiento_cuenta_fecha ON movimiento (numero_cuenta, fecha);