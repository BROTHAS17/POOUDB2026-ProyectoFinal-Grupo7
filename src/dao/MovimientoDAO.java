package dao;

import conexion.Conexion;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    private static final Logger logger = LogManager.getLogger(MovimientoDAO.class);

    private static final String SQL_INSERTAR =
            "INSERT INTO movimientos (tipo, monto, cuenta_origen_id, cuenta_destino_id, saldo_resultante) " +
            "VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_BUSCAR_POR_ID =
            "SELECT id, tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante " +
            "FROM movimientos WHERE id = ?";

    private static final String SQL_LISTAR_POR_CUENTA =
            "SELECT id, tipo, monto, fecha, cuenta_origen_id, cuenta_destino_id, saldo_resultante " +
            "FROM movimientos WHERE cuenta_origen_id = ? OR cuenta_destino_id = ? " +
            "ORDER BY fecha DESC, id DESC";

    public int insertar(Movimiento movimiento) throws SQLException {
        try (Connection conn = Conexion.getConexion()) {
            return insertar(movimiento, conn);
        }
    }

    public int insertar(Movimiento movimiento, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, movimiento.getTipo());
            ps.setBigDecimal(2, movimiento.getMonto());

            if (movimiento.getCuentaOrigenId() != null) {
                ps.setInt(3, movimiento.getCuentaOrigenId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            if (movimiento.getCuentaDestinoId() != null) {
                ps.setInt(4, movimiento.getCuentaDestinoId());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            ps.setBigDecimal(5, movimiento.getSaldoResultante());

            int afectadas = ps.executeUpdate();
            if (afectadas == 0) {
                return -1;
            }

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    movimiento.setId(idGenerado);
                    logger.info("Movimiento registrado [id={}, tipo={}, monto={}, saldoResultante={}]",
                            idGenerado, movimiento.getTipo(), movimiento.getMonto(), movimiento.getSaldoResultante());
                    return idGenerado;
                }
            }
            return -1;
        } catch (SQLException e) {
            logger.error("Fallo al insertar movimiento [tipo={}, monto={}]: {}",
                    movimiento.getTipo(), movimiento.getMonto(), e.getMessage(), e);
            throw e;
        }
    }

    public Movimiento buscarPorId(int id) throws SQLException {
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_BUSCAR_POR_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearFila(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Fallo al consultar movimiento por id={}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    public List<Movimiento> listarPorCuenta(int idCuenta) throws SQLException {
        List<Movimiento> resultados = new ArrayList<>();
        try (Connection conn = Conexion.getConexion();
             PreparedStatement ps = conn.prepareStatement(SQL_LISTAR_POR_CUENTA)) {
            ps.setInt(1, idCuenta);
            ps.setInt(2, idCuenta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resultados.add(mapearFila(rs));
                }
            }
            return resultados;
        } catch (SQLException e) {
            logger.error("Fallo al listar movimientos para id_cuenta={}: {}", idCuenta, e.getMessage(), e);
            throw e;
        }
    }

    private Movimiento mapearFila(ResultSet rs) throws SQLException {
        Movimiento mov = new Movimiento();
        mov.setId(rs.getInt("id"));
        mov.setTipo(rs.getString("tipo"));
        mov.setMonto(rs.getBigDecimal("monto"));

        Timestamp ts = rs.getTimestamp("fecha");
        if (ts != null) {
            mov.setFecha(ts.toLocalDateTime());
        }

        int origen = rs.getInt("cuenta_origen_id");
        mov.setCuentaOrigenId(rs.wasNull() ? null : origen);

        int destino = rs.getInt("cuenta_destino_id");
        mov.setCuentaDestinoId(rs.wasNull() ? null : destino);

        mov.setSaldoResultante(rs.getBigDecimal("saldo_resultante"));
        return mov;
    }
}
