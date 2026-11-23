-- Script oficial de la base de datos del Banco Académico UDB (Fase 1).

CREATE DATABASE IF NOT EXISTS banco_academico_udb
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE banco_academico_udb;

-- Usuario del programa: debe coincidir con src/db.properties.
CREATE USER IF NOT EXISTS 'udb_general'@'localhost' IDENTIFIED BY 'ClaveUDB2026*';
GRANT ALL PRIVILEGES ON banco_academico_udb.* TO 'udb_general'@'localhost';
FLUSH PRIVILEGES;

CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    codigo_cliente VARCHAR(15) NOT NULL UNIQUE,
    dui CHAR(10) NOT NULL UNIQUE,
    nombre_completo VARCHAR(120) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    correo VARCHAR(120) NOT NULL,
    direccion VARCHAR(200) NOT NULL,
    estado ENUM('ACTIVO', 'INACTIVO', 'BLOQUEADO') NOT NULL DEFAULT 'ACTIVO'
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cuentas (
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

-- Datos de prueba
INSERT IGNORE INTO clientes (codigo_cliente, dui, nombre_completo, telefono, correo, direccion)
VALUES
    ('CLI001', '12345678-9', 'Ana Lopez', '7000-0001', 'ana.lopez@example.com', 'San Salvador'),
    ('CLI002', '98765432-1', 'Carlos Martinez', '7000-0002', 'carlos.martinez@example.com', 'Santa Ana');

INSERT IGNORE INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, id_cliente)
SELECT 'CTA001', 'AHORRO', '2026-11-05', id_cliente
FROM clientes
WHERE codigo_cliente = 'CLI001';
CREATE TABLE IF NOT EXISTS movimientos (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('DEPOSITO', 'RETIRO', 'TRANSFERENCIA') NOT NULL,
    monto DECIMAL(12, 2) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cuenta_origen_id INT UNSIGNED NULL,
    cuenta_destino_id INT UNSIGNED NULL,
    saldo_resultante DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_movimientos_cuenta_origen
        FOREIGN KEY (cuenta_origen_id) REFERENCES cuentas(id_cuenta)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_movimientos_cuenta_destino
        FOREIGN KEY (cuenta_destino_id) REFERENCES cuentas(id_cuenta)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_movimientos_monto_positivo
        CHECK (monto > 0.00),
    CONSTRAINT chk_movimientos_saldo_no_negativo
        CHECK (saldo_resultante >= 0.00),
    INDEX idx_movimientos_origen (cuenta_origen_id, fecha),
    INDEX idx_movimientos_destino (cuenta_destino_id, fecha),
    INDEX idx_movimientos_tipo_fecha (tipo, fecha)
) ENGINE=InnoDB;

-- Cuentas de prueba en estados ACTIVA, INACTIVA y BLOQUEADA
-- Los saldos de CTA-PRB-01 (1300.00) y CTA-PRB-02 (1000.00) reflejan los movimientos iniciales (incluyendo la transferencia de 200.00)
INSERT INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, saldo, estado, id_cliente)
SELECT 'CTA-PRB-01', 'AHORRO', CURDATE(), 1300.00, 'ACTIVA', c.id_cliente
FROM clientes c ORDER BY c.id_cliente ASC LIMIT 1
ON DUPLICATE KEY UPDATE numero_cuenta = numero_cuenta;

INSERT INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, saldo, estado, id_cliente)
SELECT 'CTA-PRB-02', 'CORRIENTE', CURDATE(), 1000.00, 'ACTIVA', c.id_cliente
FROM clientes c ORDER BY c.id_cliente ASC LIMIT 1
ON DUPLICATE KEY UPDATE numero_cuenta = numero_cuenta;

INSERT INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, saldo, estado, id_cliente)
SELECT 'CTA-PRB-03', 'AHORRO', CURDATE(), 350.00, 'INACTIVA', c.id_cliente
FROM clientes c ORDER BY c.id_cliente ASC LIMIT 1
ON DUPLICATE KEY UPDATE numero_cuenta = numero_cuenta;

INSERT INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, saldo, estado, id_cliente)
SELECT 'CTA-PRB-04', 'CORRIENTE', CURDATE(), 600.00, 'BLOQUEADA', c.id_cliente
FROM clientes c ORDER BY c.id_cliente ASC LIMIT 1
ON DUPLICATE KEY UPDATE numero_cuenta = numero_cuenta;

-- Movimientos iniciales de prueba
INSERT INTO movimientos (tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante)
SELECT 'DEPOSITO', 350.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, c.id_cuenta, 350.00
FROM cuentas c
WHERE c.numero_cuenta = 'CTA-PRB-03'
  AND NOT EXISTS (
      SELECT 1 FROM movimientos m
      WHERE m.tipo = 'DEPOSITO' AND m.cuenta_destino_id = c.id_cuenta AND m.monto = 350.00
  );

INSERT INTO movimientos (tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante)
SELECT 'DEPOSITO', 600.00, DATE_SUB(NOW(), INTERVAL 3 DAY), NULL, c.id_cuenta, 600.00
FROM cuentas c
WHERE c.numero_cuenta = 'CTA-PRB-04'
  AND NOT EXISTS (
      SELECT 1 FROM movimientos m
      WHERE m.tipo = 'DEPOSITO' AND m.cuenta_destino_id = c.id_cuenta AND m.monto = 600.00
  );

INSERT INTO movimientos (tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante)
SELECT 'DEPOSITO', 1500.00, DATE_SUB(NOW(), INTERVAL 2 DAY), NULL, c.id_cuenta, 1500.00
FROM cuentas c
WHERE c.numero_cuenta = 'CTA-PRB-01'
  AND NOT EXISTS (
      SELECT 1 FROM movimientos m
      WHERE m.tipo = 'DEPOSITO' AND m.cuenta_destino_id = c.id_cuenta AND m.monto = 1500.00
  );

INSERT INTO movimientos (tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante)
SELECT 'DEPOSITO', 800.00, DATE_SUB(NOW(), INTERVAL 1 DAY), NULL, c.id_cuenta, 800.00
FROM cuentas c
WHERE c.numero_cuenta = 'CTA-PRB-02'
  AND NOT EXISTS (
      SELECT 1 FROM movimientos m
      WHERE m.tipo = 'DEPOSITO' AND m.cuenta_destino_id = c.id_cuenta AND m.monto = 800.00
  );

INSERT INTO movimientos (tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante)
SELECT 'TRANSFERENCIA', 200.00, NOW(), origen.id_cuenta, destino.id_cuenta, 1300.00
FROM cuentas origen
JOIN cuentas destino ON destino.numero_cuenta = 'CTA-PRB-02'
WHERE origen.numero_cuenta = 'CTA-PRB-01'
  AND NOT EXISTS (
      SELECT 1 FROM movimientos m
      WHERE m.tipo = 'TRANSFERENCIA'
        AND m.cuenta_origen_id = origen.id_cuenta
        AND m.cuenta_destino_id = destino.id_cuenta
        AND m.monto = 200.00
  );
