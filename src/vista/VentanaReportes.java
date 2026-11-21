package vista;

import dao.ReporteDAO;
import modelo.Cliente;
import modelo.CuentaBancaria;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class VentanaReportes extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaReportes.class);

    private final ReporteDAO reporteDAO;

    private JTabbedPane pestanas;

    // Pestaña: Cuentas activas
    private JComboBox<String> cbxTipoCuentasActivas;
    private DefaultTableModel modeloCuentasActivas;
    private JLabel lblResumenActivas;

    // Pestaña: Cuentas bloqueadas
    private JComboBox<String> cbxTipoCuentasBloqueadas;
    private DefaultTableModel modeloCuentasBloqueadas;
    private JLabel lblResumenBloqueadas;

    // Pestaña: Clientes registrados
    private JComboBox<String> cbxEstadoClientes;
    private JTextField txtBuscarCliente;
    private DefaultTableModel modeloClientes;
    private JLabel lblResumenClientes;

    public VentanaReportes() {
        this.reporteDAO = new ReporteDAO();
        initComponents();
        cargarTodosLosReportes();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Gestión de Reportes");
        setSize(1050, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = new JPanel(new BorderLayout(10, 10));
        panelSuperior.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));

        JLabel lblTitulo = new JLabel("Módulo de Reportes Bancarios");
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));

        JButton btnActualizarTodo = new JButton("Actualizar todos los reportes");
        btnActualizarTodo.addActionListener(e -> cargarTodosLosReportes());

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(btnActualizarTodo, BorderLayout.EAST);

        pestanas = new JTabbedPane();
        pestanas.addTab("Cuentas Activas", crearPanelCuentasActivas());
        pestanas.addTab("Cuentas Bloqueadas", crearPanelCuentasBloqueadas());
        pestanas.addTab("Clientes Registrados", crearPanelClientesRegistrados());

        add(panelSuperior, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
    }

    private JPanel crearPanelCuentasActivas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel("Tipo de cuenta:"));
        cbxTipoCuentasActivas = new JComboBox<>(new String[]{"TODOS", "AHORRO", "CORRIENTE"});
        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> cargarCuentasActivas());

        filtros.add(cbxTipoCuentasActivas);
        filtros.add(btnConsultar);

        modeloCuentasActivas = crearModeloTablaNoEditable(
                new Object[]{"ID", "Número", "Tipo", "Fecha Apertura", "Saldo", "Estado", "Cliente"}
        );
        JTable tabla = new JTable(modeloCuentasActivas);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado de Cuentas Activas"));

        lblResumenActivas = new JLabel("Total de cuentas activas: 0 | Suma de saldos: $0.00");
        lblResumenActivas.setFont(lblResumenActivas.getFont().deriveFont(Font.BOLD));

        panel.add(filtros, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblResumenActivas, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelCuentasBloqueadas() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel("Tipo de cuenta:"));
        cbxTipoCuentasBloqueadas = new JComboBox<>(new String[]{"TODOS", "AHORRO", "CORRIENTE"});
        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> cargarCuentasBloqueadas());

        filtros.add(cbxTipoCuentasBloqueadas);
        filtros.add(btnConsultar);

        modeloCuentasBloqueadas = crearModeloTablaNoEditable(
                new Object[]{"ID", "Número", "Tipo", "Fecha Apertura", "Saldo", "Estado", "Cliente"}
        );
        JTable tabla = new JTable(modeloCuentasBloqueadas);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Listado de Cuentas Bloqueadas"));

        lblResumenBloqueadas = new JLabel("Total de cuentas bloqueadas: 0 | Suma de saldos: $0.00");
        lblResumenBloqueadas.setFont(lblResumenBloqueadas.getFont().deriveFont(Font.BOLD));

        panel.add(filtros, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblResumenBloqueadas, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelClientesRegistrados() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel("Estado:"));
        cbxEstadoClientes = new JComboBox<>(new String[]{"TODOS", "ACTIVO", "INACTIVO", "BLOQUEADO"});
        filtros.add(cbxEstadoClientes);

        filtros.add(new JLabel("Buscar (código, DUI o nombre):"));
        txtBuscarCliente = new JTextField(18);
        filtros.add(txtBuscarCliente);

        JButton btnBuscar = new JButton("Filtrar");
        btnBuscar.addActionListener(e -> cargarClientesRegistrados());
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            cbxEstadoClientes.setSelectedIndex(0);
            txtBuscarCliente.setText("");
            cargarClientesRegistrados();
        });

        filtros.add(btnBuscar);
        filtros.add(btnLimpiar);

        modeloClientes = crearModeloTablaNoEditable(
                new Object[]{"ID", "Código", "DUI", "Nombre Completo", "Teléfono", "Correo", "Dirección", "Estado"}
        );
        JTable tabla = new JTable(modeloClientes);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Clientes Registrados"));

        lblResumenClientes = new JLabel("Total de clientes registrados: 0");
        lblResumenClientes.setFont(lblResumenClientes.getFont().deriveFont(Font.BOLD));

        panel.add(filtros, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblResumenClientes, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarTodosLosReportes() {
        logger.info("Actualizando reportes desde VentanaReportes");
        cargarCuentasActivas();
        cargarCuentasBloqueadas();
        cargarClientesRegistrados();
    }

    private void cargarCuentasActivas() {
        modeloCuentasActivas.setRowCount(0);
        String tipo = (String) cbxTipoCuentasActivas.getSelectedItem();
        try {
            List<CuentaBancaria> cuentas = reporteDAO.listarCuentasPorEstado("ACTIVA", tipo);
            BigDecimal sumaSaldos = BigDecimal.ZERO;
            for (CuentaBancaria c : cuentas) {
                BigDecimal saldo = c.getSaldo() != null ? c.getSaldo() : BigDecimal.ZERO;
                sumaSaldos = sumaSaldos.add(saldo);
                modeloCuentasActivas.addRow(new Object[]{
                        c.getIdCuenta(),
                        c.getNumeroCuenta(),
                        c.getTipoCuenta(),
                        c.getFechaApertura(),
                        formatearMonto(saldo),
                        c.getEstado(),
                        c.getNombreCliente()
                });
            }
            lblResumenActivas.setText("Total de cuentas activas: " + cuentas.size()
                    + " | Suma de saldos: " + formatearMonto(sumaSaldos));
        } catch (SQLException ex) {
            logger.error("Error al cargar reporte de cuentas activas: {}", ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar reporte de cuentas activas", ex);
        }
    }

    private void cargarCuentasBloqueadas() {
        modeloCuentasBloqueadas.setRowCount(0);
        String tipo = (String) cbxTipoCuentasBloqueadas.getSelectedItem();
        try {
            List<CuentaBancaria> cuentas = reporteDAO.listarCuentasPorEstado("BLOQUEADA", tipo);
            BigDecimal sumaSaldos = BigDecimal.ZERO;
            for (CuentaBancaria c : cuentas) {
                BigDecimal saldo = c.getSaldo() != null ? c.getSaldo() : BigDecimal.ZERO;
                sumaSaldos = sumaSaldos.add(saldo);
                modeloCuentasBloqueadas.addRow(new Object[]{
                        c.getIdCuenta(),
                        c.getNumeroCuenta(),
                        c.getTipoCuenta(),
                        c.getFechaApertura(),
                        formatearMonto(saldo),
                        c.getEstado(),
                        c.getNombreCliente()
                });
            }
            lblResumenBloqueadas.setText("Total de cuentas bloqueadas: " + cuentas.size()
                    + " | Suma de saldos: " + formatearMonto(sumaSaldos));
        } catch (SQLException ex) {
            logger.error("Error al cargar reporte de cuentas bloqueadas: {}", ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar reporte de cuentas bloqueadas", ex);
        }
    }

    private void cargarClientesRegistrados() {
        modeloClientes.setRowCount(0);
        String estado = (String) cbxEstadoClientes.getSelectedItem();
        String busqueda = txtBuscarCliente.getText();
        try {
            List<Cliente> clientes = reporteDAO.listarClientesRegistrados(estado, busqueda);
            for (Cliente c : clientes) {
                modeloClientes.addRow(new Object[]{
                        c.getIdCliente(),
                        c.getCodigoCliente(),
                        c.getDui(),
                        c.getNombreCompleto(),
                        c.getTelefono(),
                        c.getCorreo(),
                        c.getDireccion(),
                        c.getEstado()
                });
            }
            lblResumenClientes.setText("Total de clientes registrados: " + clientes.size());
        } catch (SQLException ex) {
            logger.error("Error al cargar reporte de clientes registrados: {}", ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar reporte de clientes registrados", ex);
        }
    }

    private DefaultTableModel crearModeloTablaNoEditable(Object[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private String formatearMonto(BigDecimal monto) {
        BigDecimal valor = monto != null ? monto : BigDecimal.ZERO;
        return "$" + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private void mostrarErrorSql(String contexto, SQLException ex) {
        JOptionPane.showMessageDialog(this, contexto + ": " + ex.getMessage(),
                "Error SQL", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaReportes().setVisible(true));
    }
}
