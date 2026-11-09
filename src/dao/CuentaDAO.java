package dao;

import conexion.Conexion;
import modelo.CuentaBancaria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO {

    public boolean registrar(CuentaBancaria c) throws SQLException {
        String sql = "INSERT INTO cuentas (numero_cuenta, tipo_cuenta, fecha_apertura, saldo, estado, id_cliente) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNumeroCuenta().trim());
            ps.setString(2, c.getTipoCuenta().trim());
            ps.setDate(3, c.getFechaApertura());
            ps.setBigDecimal(4, c.getSaldo());
            ps.setString(5, c.getEstado().trim());
            ps.setInt(6, c.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    public List<CuentaBancaria> listar() throws SQLException {
        List<CuentaBancaria> lista = new ArrayList<>();
        String sql = "SELECT c.id_cuenta, c.numero_cuenta, c.tipo_cuenta, c.fecha_apertura, c.saldo, c.estado, c.id_cliente, cl.nombre_completo "
                + "FROM cuentas c "
                + "INNER JOIN clientes cl ON c.id_cliente = cl.id_cliente "
                + "ORDER BY c.id_cuenta ASC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                CuentaBancaria c = new CuentaBancaria(
                        rs.getInt("id_cuenta"),
                        rs.getString("numero_cuenta"),
                        rs.getString("tipo_cuenta"),
                        rs.getDate("fecha_apertura"),
                        rs.getBigDecimal("saldo"),
                        rs.getString("estado"),
                        rs.getInt("id_cliente"),
                        rs.getString("nombre_completo")
                );
                lista.add(c);
            }
        }
        return lista;
    }

    public boolean existeNumero(String numeroCuenta) throws SQLException {
        String sql = "SELECT COUNT(*) FROM cuentas WHERE numero_cuenta = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numeroCuenta.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}