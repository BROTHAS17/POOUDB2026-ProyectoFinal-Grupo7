CREATE DATABASE IF NOT EXISTS banco_academico_udb
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE banco_academico_udb;

-- 1. Crear el usuario general (reemplaza 'udb_general' y 'ClaveUDB2026*' si lo deseas)
CREATE USER 'udb_general'@'localhost' IDENTIFIED BY 'ClaveUDB2026*';

-- 2. Otorgar todos los permisos necesarios únicamente sobre la base de datos del proyecto
GRANT ALL PRIVILEGES ON banco_academico_udb.* TO 'udb_general'@'localhost';

-- 3. Refrescar los privilegios para que los cambios surtan efecto de inmediato
FLUSH PRIVILEGES;

CREATE TABLE clientes (
    id_cliente INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo_cliente VARCHAR(15) NOT NULL UNIQUE,
    dui CHAR(10) NOT NULL UNIQUE,
    nombre_completo VARCHAR(120) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO', 'BLOQUEADO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;

CREATE TABLE cuentas (
    id_cuenta INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    numero_cuenta VARCHAR(20) NOT NULL UNIQUE,
    tipo_cuenta ENUM('AHORRO', 'CORRIENTE') NOT NULL,
    fecha_apertura DATE NOT NULL,
    saldo DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    estado ENUM('ACTIVA', 'INACTIVA', 'BLOQUEADA') NOT NULL DEFAULT 'ACTIVA',
    id_cliente INT UNSIGNED NOT NULL,
    CONSTRAINT fk_cuentas_clientes
        FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
) ENGINE=InnoDB;

INSERT INTO clientes (codigo_cliente, dui, nombre_completo, telefono, correo, direccion)
VALUES
    ('CLI001', '12345678-9', 'Ana Lopez', '7000-0001', 'ana.lopez@example.com', 'San Salvador'),
    ('CLI002', '98765432-1', 'Carlos Martinez', '7000-0002', 'carlos.martinez@example.com', 'Santa Ana');

INSERT INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, id_cliente)
VALUES
    ('CTA001', 'AHORRO', '2026-11-05', 1);