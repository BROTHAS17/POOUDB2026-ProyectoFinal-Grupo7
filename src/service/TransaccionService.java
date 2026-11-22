package service;
import conexion.Conexion;
import dao.CuentaDAO;
import dao.MovimientoDAO;
import excepciones.CuentaInactivaException;
import excepciones.CuentaNoEncontradaException;
import excepciones.MontoInvalidoException;
import excepciones.SaldoInsuficienteException;
import modelo.CuentaBancaria;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class TransaccionService {

    private static final Logger logger = LogManager.getLogger(TransaccionService.class);
    private final CuentaDAO cuentaDAO;
    private final MovimientoDAO movimientoDAO;

    public TransaccionService() {
        this.cuentaDAO = new CuentaDAO();
        this.movimientoDAO = new MovimientoDAO();
    }

    /**
     * Busca una cuenta bancaria por su número de cuenta.
     */
    public CuentaBancaria buscarCuentaPorNumero(String numeroCuenta)
            throws CuentaNoEncontradaException, SQLException {
        if (numeroCuenta == null || numeroCuenta.trim().isEmpty()) {
            throw new CuentaNoEncontradaException("Debe ingresar un número de cuenta.");
        }

        List<CuentaBancaria> cuentas = cuentaDAO.listar();
        for (CuentaBancaria c : cuentas) {
            if (c.getNumeroCuenta().equalsIgnoreCase(numeroCuenta.trim())) {
                return c;
            }
        }

        logger.warn("Intento de operación sobre cuenta inexistente: {}", numeroCuenta);
        throw new CuentaNoEncontradaException("No se encontró ninguna cuenta con el número: " + numeroCuenta);
    }

    /**
     * Realiza un depósito validando reglas de negocio y garantizando atomicidad.
     */
    public Movimiento realizarDeposito(String numeroCuenta, BigDecimal monto)
            throws MontoInvalidoException, CuentaNoEncontradaException, CuentaInactivaException, SQLException {

        // 1. Validar monto > 0
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Intento de depósito con monto inválido [cuenta={}, monto={}]", numeroCuenta, monto);
            throw new MontoInvalidoException("El monto a depositar debe ser mayor que cero ($0.00).");
        }

        // 2. Validar existencia de cuenta
        CuentaBancaria cuenta = buscarCuentaPorNumero(numeroCuenta);

        // 3. Validar cuenta activa
        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstado())) {
            logger.warn("Intento de depósito en cuenta no activa [cuenta={}, estado={}]",
                    numeroCuenta, cuenta.getEstado());
            throw new CuentaInactivaException("La cuenta " + numeroCuenta + " no está activa (Estado: " + cuenta.getEstado() + ").");
        }

        // 4. Calcular nuevo saldo y ejecutar transacción
        BigDecimal nuevoSaldo = cuenta.getSaldo().add(monto);
        Movimiento mov = new Movimiento("DEPOSITO", monto, null, cuenta.getIdCuenta(), nuevoSaldo);

        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            cuentaDAO.actualizarSaldo(cuenta.getIdCuenta(), nuevoSaldo, conn);
            int idGenerado = movimientoDAO.insertar(mov, conn);
            mov.setId(idGenerado);

            conn.commit();
            logger.info("DEPÓSITO EXITOSO [idMov={}, cuenta={}, monto={}, saldoNuevo={}]",
                    idGenerado, numeroCuenta, monto, nuevoSaldo);
            return mov;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error al ejecutar rollback en depósito: {}", ex.getMessage(), ex);
                }
            }
            logger.error("Error SQL al procesar depósito [cuenta={}, monto={}]: {}",
                    numeroCuenta, monto, e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Error al cerrar conexión en depósito: {}", ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Realiza un retiro validando reglas de negocio, fondos suficientes y atomicidad.
     */
    public Movimiento realizarRetiro(String numeroCuenta, BigDecimal monto)
            throws MontoInvalidoException, CuentaNoEncontradaException, CuentaInactivaException,
            SaldoInsuficienteException, SQLException {

        // 1. Validar monto > 0
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Intento de retiro con monto inválido [cuenta={}, monto={}]", numeroCuenta, monto);
            throw new MontoInvalidoException("El monto a retirar debe ser mayor que cero ($0.00).");
        }

        // 2. Validar existencia de cuenta
        CuentaBancaria cuenta = buscarCuentaPorNumero(numeroCuenta);

        // 3. Validar cuenta activa
        if (!"ACTIVA".equalsIgnoreCase(cuenta.getEstado())) {
            logger.warn("Intento de retiro en cuenta no activa [cuenta={}, estado={}]",
                    numeroCuenta, cuenta.getEstado());
            throw new CuentaInactivaException("La cuenta " + numeroCuenta + " no está activa (Estado: " + cuenta.getEstado() + ").");
        }

        // 4. Validar saldo suficiente
        if (cuenta.getSaldo().compareTo(monto) < 0) {
            logger.warn("Retiro rechazado por saldo insuficiente [cuenta={}, saldoActual={}, montoSolicitado={}]",
                    numeroCuenta, cuenta.getSaldo(), monto);
            throw new SaldoInsuficienteException(
                    String.format("Saldo insuficiente. Saldo disponible: $%.2f | Monto solicitado: $%.2f",
                            cuenta.getSaldo(), monto));
        }

        // 5. Calcular nuevo saldo y ejecutar transacción
        BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(monto);
        Movimiento mov = new Movimiento("RETIRO", monto, cuenta.getIdCuenta(), null, nuevoSaldo);

        Connection conn = null;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            cuentaDAO.actualizarSaldo(cuenta.getIdCuenta(), nuevoSaldo, conn);
            int idGenerado = movimientoDAO.insertar(mov, conn);
            mov.setId(idGenerado);

            conn.commit();
            logger.info("RETIRO EXITOSO [idMov={}, cuenta={}, monto={}, saldoNuevo={}]",
                    idGenerado, numeroCuenta, monto, nuevoSaldo);
            return mov;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error al ejecutar rollback en retiro: {}", ex.getMessage(), ex);
                }
            }
            logger.error("Error SQL al procesar retiro [cuenta={}, monto={}]: {}",
                    numeroCuenta, monto, e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Error al cerrar conexión en retiro: {}", ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * Consulta un movimiento específico por su ID.
     */
    public Movimiento consultarMovimientoPorId(int idMovimiento) throws MontoInvalidoException, SQLException {
        if (idMovimiento <= 0) {
            throw new MontoInvalidoException("El ID del movimiento debe ser un número entero positivo.");
        }
        Movimiento mov = movimientoDAO.buscarPorId(idMovimiento);
        if (mov != null) {
            logger.info("Consulta de movimiento exitosa [id={}]", idMovimiento);
        } else {
            logger.info("Consulta de movimiento sin resultados [id={}]", idMovimiento);
        }
        return mov;
    }

    /**
     * Método auxiliar para mostrar el número de cuenta en lugar de solo el ID en el comprobante.
     */
    public String obtenerNumeroCuentaPorId(Integer idCuenta) throws SQLException {
        if (idCuenta == null) return "N/A";
        for (CuentaBancaria c : cuentaDAO.listar()) {
            if (c.getIdCuenta() == idCuenta) {
                return c.getNumeroCuenta() + " (" + c.getNombreCliente() + ")";
            }
        }
        return "ID: " + idCuenta;
    }
}