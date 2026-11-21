package dao;

import conexion.Conexion;
import modelo.Cliente;
import modelo.CuentaBancaria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
