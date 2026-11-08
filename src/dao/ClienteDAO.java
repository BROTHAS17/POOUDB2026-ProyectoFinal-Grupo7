package dao;

import conexion.Conexion;
import modelo.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD contra la tabla clientes creada por Persona 1.
 */
public class ClienteDAO {

    public boolean registrar(Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (codigo_cliente, dui, nombre_completo, telefono, correo, direccion, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCodigoCliente().trim());
            ps.setString(2, c.getDui().trim());
            ps.setString(3, c.getNombreCompleto().trim());
            ps.setString(4, c.getTelefono().trim());
            ps.setString(5, c.getCorreo().trim());
            ps.setString(6, c.getDireccion().trim());
            ps.setString(7, c.getEstado().trim());
            return ps.executeUpdate() > 0;
        }
    }

    public List<Cliente> listar() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT id_cliente, codigo_cliente, dui, nombre_completo, telefono, correo, direccion, estado "
                   + "FROM clientes ORDER BY id_cliente ASC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getInt("id_cliente"),
                    rs.getString("codigo_cliente"),
                    rs.getString("dui"),
                    rs.getString("nombre_completo"),
                    rs.getString("telefono"),
                    rs.getString("correo"),
                    rs.getString("direccion"),
                    rs.getString("estado")
                );
                lista.add(c);
            }
        }
        return lista;
    }

    public boolean modificar(Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET codigo_cliente = ?, dui = ?, nombre_completo = ?, telefono = ?, "
                   + "correo = ?, direccion = ?, estado = ? WHERE id_cliente = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getCodigoCliente().trim());
            ps.setString(2, c.getDui().trim());
            ps.setString(3, c.getNombreCompleto().trim());
            ps.setString(4, c.getTelefono().trim());
            ps.setString(5, c.getCorreo().trim());
            ps.setString(6, c.getDireccion().trim());
            ps.setString(7, c.getEstado().trim());
            ps.setInt(8, c.getIdCliente());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cambiarEstado(int idCliente, String nuevoEstado) throws SQLException {
        String sql = "UPDATE clientes SET estado = ? WHERE id_cliente = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idCliente);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existeCodigo(String codigo, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE codigo_cliente = ? AND id_cliente <> ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo.trim());
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public boolean existeDui(String dui, int idExcluir) throws SQLException {
        String sql = "SELECT COUNT(*) FROM clientes WHERE dui = ? AND id_cliente <> ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, dui.trim());
            ps.setInt(2, idExcluir);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}
