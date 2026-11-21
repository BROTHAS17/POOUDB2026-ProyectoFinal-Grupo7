package dao;

import conexion.Conexion;
import modelo.Cliente;
import modelo.CuentaBancaria;
import modelo.ReporteMovimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReporteDAO {

    private static final Logger logger = LogManager.getLogger(ReporteDAO.class);

    public List<CuentaBancaria> listarCuentasPorEstado(String estado, String tipoCuenta) throws SQLException {
        List<CuentaBancaria> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.id_cuenta, c.numero_cuenta, c.tipo_cuenta, c.fecha_apertura, "
                        + "c.saldo, c.estado, c.id_cliente, cl.nombre_completo "
                        + "FROM cuentas c "
                        + "INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                        + "WHERE c.estado = ?"
        );

        boolean filtrarTipo = tipoCuenta != null && !tipoCuenta.trim().isEmpty()
                && !"TODOS".equalsIgnoreCase(tipoCuenta.trim());
        if (filtrarTipo) {
            sql.append(" AND c.tipo_cuenta = ?");
        }
        sql.append(" ORDER BY c.id_cuenta ASC");

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setString(1, estado.trim().toUpperCase());
            if (filtrarTipo) {
                ps.setString(2, tipoCuenta.trim().toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CuentaBancaria cuenta = new CuentaBancaria(
                            rs.getInt("id_cuenta"),
                            rs.getString("numero_cuenta"),
                            rs.getString("tipo_cuenta"),
                            rs.getDate("fecha_apertura"),
                            rs.getBigDecimal("saldo"),
                            rs.getString("estado"),
                            rs.getInt("id_cliente"),
                            rs.getString("nombre_completo")
                    );
                    lista.add(cuenta);
                }
            }
            logger.info("Reporte de cuentas por estado consultado [estado={}, tipo={}, registros={}]",
                    estado, filtrarTipo ? tipoCuenta : "TODOS", lista.size());
            return lista;
        } catch (SQLException e) {
            logger.error("Error al consultar cuentas por estado [estado={}]: {}", estado, e.getMessage(), e);
            throw e;
        }
    }

    public List<CuentaBancaria> listarTodasLasCuentas() throws SQLException {
        List<CuentaBancaria> lista = new ArrayList<>();
        String sql = "SELECT c.id_cuenta, c.numero_cuenta, c.tipo_cuenta, c.fecha_apertura, "
                + "c.saldo, c.estado, c.id_cliente, cl.nombre_completo "
                + "FROM cuentas c "
                + "INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                + "ORDER BY c.numero_cuenta ASC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CuentaBancaria cuenta = new CuentaBancaria(
                        rs.getInt("id_cuenta"),
                        rs.getString("numero_cuenta"),
                        rs.getString("tipo_cuenta"),
                        rs.getDate("fecha_apertura"),
                        rs.getBigDecimal("saldo"),
                        rs.getString("estado"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_completo")
                );
                lista.add(cuenta);
            }
            logger.info("Listado general de cuentas para reportes consultado [registros={}]", lista.size());
            return lista;
        } catch (SQLException e) {
            logger.error("Error al listar cuentas para reportes: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<Cliente> listarClientesRegistrados(String estado, String textoBusqueda) throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id_cliente, codigo_cliente, dui, nombre_completo, telefono, correo, direccion, estado "
                        + "FROM clientes WHERE 1=1"
        );

        boolean filtrarEstado = estado != null && !estado.trim().isEmpty()
                && !"TODOS".equalsIgnoreCase(estado.trim());
        boolean filtrarTexto = textoBusqueda != null && !textoBusqueda.trim().isEmpty();

        if (filtrarEstado) {
            sql.append(" AND estado = ?");
        }
        if (filtrarTexto) {
            sql.append(" AND (codigo_cliente LIKE ? OR dui LIKE ? OR nombre_completo LIKE ?)");
        }
        sql.append(" ORDER BY id_cliente ASC");

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (filtrarEstado) {
                ps.setString(idx++, estado.trim().toUpperCase());
            }
            if (filtrarTexto) {
                String patron = "%" + textoBusqueda.trim() + "%";
                ps.setString(idx++, patron);
                ps.setString(idx++, patron);
                ps.setString(idx++, patron);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente(
                            rs.getInt("id_cliente"),
                            rs.getString("codigo_cliente"),
                            rs.getString("dui"),
                            rs.getString("nombre_completo"),
                            rs.getString("telefono"),
                            rs.getString("correo"),
                            rs.getString("direccion"),
                            rs.getString("estado")
                    );
                    lista.add(cliente);
                }
            }
            logger.info("Reporte de clientes registrados consultado [estado={}, busqueda='{}', registros={}]",
                    filtrarEstado ? estado : "TODOS", filtrarTexto ? textoBusqueda.trim() : "", lista.size());
            return lista;
        } catch (SQLException e) {
            logger.error("Error al consultar clientes registrados: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ReporteMovimiento> listarHistorialTransacciones(String tipo, LocalDate desde, LocalDate hasta)
            throws SQLException {
        List<ReporteMovimiento> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.id, m.tipo, m.monto, m.fecha, m.cuenta_origen_id, co.numero_cuenta AS num_origen, "
                        + "m.cuenta_destino_id, cd.numero_cuenta AS num_destino, m.saldo_resultante "
                        + "FROM movimientos m "
                        + "LEFT JOIN cuentas co ON m.cuenta_origen_id = co.id_cuenta "
                        + "LEFT JOIN cuentas cd ON m.cuenta_destino_id = cd.id_cuenta "
                        + "WHERE 1=1"
        );

        boolean filtrarTipo = tipo != null && !tipo.trim().isEmpty()
                && !"TODOS".equalsIgnoreCase(tipo.trim());
        if (filtrarTipo) {
            sql.append(" AND m.tipo = ?");
        }
        if (desde != null) {
            sql.append(" AND m.fecha >= ?");
        }
        if (hasta != null) {
            sql.append(" AND m.fecha < ?");
        }
        sql.append(" ORDER BY m.fecha DESC, m.id DESC");

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (filtrarTipo) {
                ps.setString(idx++, tipo.trim().toUpperCase());
            }
            if (desde != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(desde.atStartOfDay()));
            }
            if (hasta != null) {
                ps.setTimestamp(idx++, Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReporteMovimiento(rs));
                }
            }
            logger.info("Reporte de historial de transacciones consultado [tipo={}, desde={}, hasta={}, registros={}]",
                    filtrarTipo ? tipo : "TODOS", desde, hasta, lista.size());
            return lista;
        } catch (SQLException e) {
            logger.error("Error al consultar historial de transacciones: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ReporteMovimiento> listarTransaccionesPorCuenta(int idCuenta, String tipo) throws SQLException {
        List<ReporteMovimiento> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT m.id, m.tipo, m.monto, m.fecha, m.cuenta_origen_id, co.numero_cuenta AS num_origen, "
                        + "m.cuenta_destino_id, cd.numero_cuenta AS num_destino, m.saldo_resultante "
                        + "FROM movimientos m "
                        + "LEFT JOIN cuentas co ON m.cuenta_origen_id = co.id_cuenta "
                        + "LEFT JOIN cuentas cd ON m.cuenta_destino_id = cd.id_cuenta "
                        + "WHERE (m.cuenta_origen_id = ? OR m.cuenta_destino_id = ?)"
        );

        boolean filtrarTipo = tipo != null && !tipo.trim().isEmpty()
                && !"TODOS".equalsIgnoreCase(tipo.trim());
        if (filtrarTipo) {
            sql.append(" AND m.tipo = ?");
        }
        sql.append(" ORDER BY m.fecha DESC, m.id DESC");

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            ps.setInt(1, idCuenta);
            ps.setInt(2, idCuenta);
            if (filtrarTipo) {
                ps.setString(3, tipo.trim().toUpperCase());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearReporteMovimiento(rs));
                }
            }
            logger.info("Reporte de transacciones por cuenta consultado [idCuenta={}, tipo={}, registros={}]",
                    idCuenta, filtrarTipo ? tipo : "TODOS", lista.size());
            return lista;
        } catch (SQLException e) {
            logger.error("Error al consultar transacciones por cuenta [idCuenta={}]: {}", idCuenta, e.getMessage(), e);
            throw e;
        }
    }

    private ReporteMovimiento mapearReporteMovimiento(ResultSet rs) throws SQLException {
        ReporteMovimiento rm = new ReporteMovimiento();
        rm.setId(rs.getInt("id"));
        rm.setTipo(rs.getString("tipo"));
        rm.setMonto(rs.getBigDecimal("monto"));

        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) {
            rm.setFecha(ts.toLocalDateTime());
        }

        int origen = rs.getInt("cuenta_origen_id");
        rm.setCuentaOrigenId(rs.wasNull() ? null : origen);
        rm.setNumeroCuentaOrigen(rs.getString("num_origen"));

        int destino = rs.getInt("cuenta_destino_id");
        rm.setCuentaDestinoId(rs.wasNull() ? null : destino);
        rm.setNumeroCuentaDestino(rs.getString("num_destino"));

        rm.setSaldoResultante(rs.getBigDecimal("saldo_resultante"));
        return rm;
    }
}
