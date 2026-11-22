package service;
import conexion.Conexion;
import dao.CuentaDAO;
import dao.MovimientoDAO;
import excepciones.CuentaInactivaException;
import excepciones.CuentaNoEncontradaException;
import excepciones.MontoInvalidoException;
import excepciones.OperacionNoPermitidaException;
import excepciones.SaldoInsuficienteException;
import modelo.CuentaBancaria;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * revisa las reglas de la transferencia que no necesitan la base de datos, que ORIGEN y DESTINO
     * no estén vacíos, que MONTO sea mayor que cero y tenga como máximo dos decimales
     * y que ORIGEN y DESTINO no sean la misma cuenta,
     * la ventana lo llama antes de pedir la confirmación y el método de la transferencia lo vuelve a llamar
     * para que la regla se cumpla aunque otra clase use el servicio
     */
    public void validarDatosTransferencia(String numeroOrigen, String numeroDestino, BigDecimal monto)
            throws CuentaNoEncontradaException, MontoInvalidoException, OperacionNoPermitidaException {

        String origen = numeroOrigen == null ? "" : numeroOrigen.trim();
        String destino = numeroDestino == null ? "" : numeroDestino.trim();

        if (origen.isEmpty() || destino.isEmpty()) {
            throw new CuentaNoEncontradaException("Debe ingresar el número de la cuenta de origen y el de la cuenta de destino.");
        }
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Transferencia rechazada: monto inválido [origen={}, destino={}, monto={}]", origen, destino, monto);
            throw new MontoInvalidoException("El monto a transferir debe ser mayor que cero ($0.00).");
        }
        if (monto.stripTrailingZeros().scale() > 2) {
            logger.warn("Transferencia rechazada: monto con más de dos decimales [origen={}, destino={}, monto={}]",
                    origen, destino, monto);
            throw new MontoInvalidoException("El monto no puede tener más de dos decimales.");
        }
        if (origen.equalsIgnoreCase(destino)) {
            logger.warn("Transferencia rechazada: origen y destino son la misma cuenta [cuenta={}]", origen);
            throw new OperacionNoPermitidaException("La cuenta de origen y la cuenta de destino no pueden ser la misma.");
        }
    }

    /**
     * realiza una transferencia entre dos cuentas,
     * las dos cuentas deben existir, ser distintas y estar activas, MONTO debe ser mayor que cero
     * con un máximo de dos decimales y la cuenta de origen debe tener saldo suficiente,
     * el descuento, el abono y el registro del movimiento se hacen en una sola transacción,
     * si cualquier paso falla se ejecuta rollback y ningún saldo cambia,
     * las dos cuentas se leen con select for update dentro de la transacción, así el estado y el saldo
     * se validan en el momento de transferir y no cuando se abrió la ventana,
     * el movimiento se guarda una sola vez con la cuenta de origen y la de destino
     * y su SALDO_RESULTANTE es el saldo nuevo de la cuenta de origen
     */
    public Movimiento realizarTransferencia(String numeroOrigen, String numeroDestino, BigDecimal monto)
            throws MontoInvalidoException, OperacionNoPermitidaException, CuentaNoEncontradaException,
            CuentaInactivaException, SaldoInsuficienteException, SQLException {

        // 1) validaciones que no necesitan la base de datos
        validarDatosTransferencia(numeroOrigen, numeroDestino, monto);
        String origen = numeroOrigen.trim();
        String destino = numeroDestino.trim();

        Connection conn = null;
        boolean confirmada = false;
        try {
            conn = Conexion.getConexion();
            conn.setAutoCommit(false);

            // 2) leer y bloquear las dos cuentas dentro de la transacción
            CuentaBancaria cuentaOrigen = cuentaDAO.buscarPorNumeroParaActualizar(origen, conn);
            if (cuentaOrigen == null) {
                logger.warn("Transferencia rechazada: la cuenta de origen no existe [origen={}]", origen);
                throw new CuentaNoEncontradaException("No existe la cuenta de origen: " + origen);
            }
            CuentaBancaria cuentaDestino = cuentaDAO.buscarPorNumeroParaActualizar(destino, conn);
            if (cuentaDestino == null) {
                logger.warn("Transferencia rechazada: la cuenta de destino no existe [destino={}]", destino);
                throw new CuentaNoEncontradaException("No existe la cuenta de destino: " + destino);
            }

            // 3) ambas cuentas deben estar activas
            if (!"ACTIVA".equalsIgnoreCase(cuentaOrigen.getEstado())) {
                logger.warn("Transferencia rechazada: cuenta de origen no activa [origen={}, estado={}]",
                        origen, cuentaOrigen.getEstado());
                throw new CuentaInactivaException("La cuenta de origen " + cuentaOrigen.getNumeroCuenta()
                        + " no está activa (Estado: " + cuentaOrigen.getEstado() + ").");
            }
            if (!"ACTIVA".equalsIgnoreCase(cuentaDestino.getEstado())) {
                logger.warn("Transferencia rechazada: cuenta de destino no activa [destino={}, estado={}]",
                        destino, cuentaDestino.getEstado());
                throw new CuentaInactivaException("La cuenta de destino " + cuentaDestino.getNumeroCuenta()
                        + " no está activa (Estado: " + cuentaDestino.getEstado() + ").");
            }

            // 4) la cuenta de origen debe tener saldo suficiente
            if (cuentaOrigen.getSaldo().compareTo(monto) < 0) {
                logger.warn("Transferencia rechazada: saldo insuficiente [origen={}, saldoActual={}, montoSolicitado={}]",
                        origen, cuentaOrigen.getSaldo(), monto);
                throw new SaldoInsuficienteException(
                        String.format("Saldo insuficiente en la cuenta de origen. Saldo disponible: $%.2f | Monto solicitado: $%.2f",
                                cuentaOrigen.getSaldo(), monto));
            }

            // 5) descontar, abonar y registrar el movimiento
            BigDecimal nuevoSaldoOrigen = cuentaOrigen.getSaldo().subtract(monto);
            BigDecimal nuevoSaldoDestino = cuentaDestino.getSaldo().add(monto);

            cuentaDAO.actualizarSaldo(cuentaOrigen.getIdCuenta(), nuevoSaldoOrigen, conn);
            cuentaDAO.actualizarSaldo(cuentaDestino.getIdCuenta(), nuevoSaldoDestino, conn);

            Movimiento mov = new Movimiento("TRANSFERENCIA", monto,
                    cuentaOrigen.getIdCuenta(), cuentaDestino.getIdCuenta(), nuevoSaldoOrigen);
            int idGenerado = movimientoDAO.insertar(mov, conn);
            if (idGenerado <= 0) {
                throw new SQLException("No se pudo registrar el movimiento de la transferencia.");
            }

            conn.commit();
            confirmada = true;
            logger.info("TRANSFERENCIA EXITOSA [idMov={}, origen={}, destino={}, monto={}, saldoOrigen={}, saldoDestino={}]",
                    idGenerado, cuentaOrigen.getNumeroCuenta(), cuentaDestino.getNumeroCuenta(),
                    monto, nuevoSaldoOrigen, nuevoSaldoDestino);
            return mov;

        } catch (SQLException e) {
            logger.error("Error SQL al procesar transferencia [origen={}, destino={}, monto={}]: {}",
                    origen, destino, monto, e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                // si CONFIRMADA sigue en false no se llegó al commit por una regla incumplida o un error, se deshace todo
                if (!confirmada) {
                    try {
                        conn.rollback();
                    } catch (SQLException ex) {
                        logger.error("Error al ejecutar rollback en transferencia: {}", ex.getMessage(), ex);
                    }
                }
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Error al cerrar conexión en transferencia: {}", ex.getMessage(), ex);
                }
            }
        }
    }

    /**
     * devuelve los movimientos de CUENTA del más reciente al más antiguo,
     * incluye los movimientos donde la cuenta es origen y donde es destino
     */
    public List<Movimiento> obtenerHistorial(CuentaBancaria cuenta) throws SQLException {
        List<Movimiento> movimientos = movimientoDAO.listarPorCuenta(cuenta.getIdCuenta());
        logger.info("Historial consultado [cuenta={}, movimientos={}]", cuenta.getNumeroCuenta(), movimientos.size());
        return movimientos;
    }

    /**
     * relaciona el id de cada cuenta con su número para mostrar números de cuenta
     * en el historial sin hacer una consulta por cada fila
     */
    public Map<Integer, String> obtenerNumerosDeCuenta() throws SQLException {
        Map<Integer, String> numeros = new HashMap<>();
        for (CuentaBancaria c : cuentaDAO.listar()) {
            numeros.put(c.getIdCuenta(), c.getNumeroCuenta());
        }
        return numeros;
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