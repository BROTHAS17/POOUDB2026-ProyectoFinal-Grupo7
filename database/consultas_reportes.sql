-- Consultas SQL oficiales del Módulo de Reportes (Fase 2 - Persona 4)
-- Base de datos: banco_academico_udb

USE banco_academico_udb;

-- 1. Reporte: Cuentas activas (con nombre del cliente propietario)
SELECT c.id_cuenta, c.numero_cuenta, c.tipo_cuenta, c.fecha_apertura,
       c.saldo, c.estado, c.id_cliente, cl.nombre_completo
FROM cuentas c
INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente
WHERE c.estado = 'ACTIVA'
ORDER BY c.id_cuenta ASC;

-- 2. Reporte: Cuentas bloqueadas (con nombre del cliente propietario)
SELECT c.id_cuenta, c.numero_cuenta, c.tipo_cuenta, c.fecha_apertura,
       c.saldo, c.estado, c.id_cliente, cl.nombre_completo
FROM cuentas c
INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente
WHERE c.estado = 'BLOQUEADA'
ORDER BY c.id_cuenta ASC;

-- 3. Reporte: Clientes registrados
SELECT id_cliente, codigo_cliente, dui, nombre_completo,
       telefono, correo, direccion, estado
FROM clientes
ORDER BY id_cliente ASC;

-- 4. Reporte: Historial general de transacciones
SELECT m.id, m.tipo, m.monto, m.fecha,
       m.cuenta_origen_id, co.numero_cuenta AS num_origen,
       m.cuenta_destino_id, cd.numero_cuenta AS num_destino,
       m.saldo_resultante
FROM movimientos m
LEFT JOIN cuentas co ON m.cuenta_origen_id = co.id_cuenta
LEFT JOIN cuentas cd ON m.cuenta_destino_id = cd.id_cuenta
ORDER BY m.fecha DESC, m.id DESC;

-- 5. Reporte: Transacciones por cuenta (ejemplo: CTA-PRB-01)
SELECT m.id, m.tipo, m.monto, m.fecha,
       m.cuenta_origen_id, co.numero_cuenta AS num_origen,
       m.cuenta_destino_id, cd.numero_cuenta AS num_destino,
       m.saldo_resultante
FROM movimientos m
LEFT JOIN cuentas co ON m.cuenta_origen_id = co.id_cuenta
LEFT JOIN cuentas cd ON m.cuenta_destino_id = cd.id_cuenta
WHERE m.cuenta_origen_id = (SELECT id_cuenta FROM cuentas WHERE numero_cuenta = 'CTA-PRB-01')
   OR m.cuenta_destino_id = (SELECT id_cuenta FROM cuentas WHERE numero_cuenta = 'CTA-PRB-01')
ORDER BY m.fecha DESC, m.id DESC;

-- 6. Reporte: Total depositado
SELECT COALESCE(SUM(monto), 0.00) AS total_depositado
FROM movimientos
WHERE tipo = 'DEPOSITO';

-- 7. Reporte: Total retirado
SELECT COALESCE(SUM(monto), 0.00) AS total_retirado
FROM movimientos
WHERE tipo = 'RETIRO';

-- 8. Reporte: Total transferido
SELECT COALESCE(SUM(monto), 0.00) AS total_transferido
FROM movimientos
WHERE tipo = 'TRANSFERENCIA';

-- Resumen consolidado de totales y cantidad de operaciones por tipo
SELECT tipo,
       COUNT(*) AS cantidad_movimientos,
       COALESCE(SUM(monto), 0.00) AS monto_total
FROM movimientos
GROUP BY tipo;
