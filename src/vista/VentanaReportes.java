package vista;

import dao.ReporteDAO;
import modelo.Cliente;
import modelo.CuentaBancaria;
import modelo.ReporteMovimiento;
import modelo.ResumenTotales;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class VentanaReportes extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaReportes.class);
    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final ReporteDAO reporteDAO;

    private JTabbedPane pestanas;

    // Pestaña: Totales (depositado, retirado, transferido)
    private JTextField txtFechaDesdeTotales;
    private JTextField txtFechaHastaTotales;
    private JLabel lblTotalDepositado;
    private JLabel lblTotalRetirado;
    private JLabel lblTotalTransferido;
    private DefaultTableModel modeloTotales;
    private JLabel lblResumenTotales;

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

    // Pestaña: Historial de transacciones
    private JComboBox<String> cbxTipoHistorial;
    private JTextField txtFechaDesdeHistorial;
    private JTextField txtFechaHastaHistorial;
    private DefaultTableModel modeloHistorial;
    private JLabel lblResumenHistorial;

    // Pestaña: Transacciones por cuenta
    private JComboBox<CuentaItem> cbxCuentaReporte;
    private JComboBox<String> cbxTipoPorCuenta;
    private JLabel lblInfoCuentaSeleccionada;
    private DefaultTableModel modeloPorCuenta;
    private JLabel lblResumenPorCuenta;

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
        pestanas.addTab("Totales", crearPanelTotales());
        pestanas.addTab("Cuentas Activas", crearPanelCuentasActivas());
        pestanas.addTab("Cuentas Bloqueadas", crearPanelCuentasBloqueadas());
        pestanas.addTab("Clientes Registrados", crearPanelClientesRegistrados());
        pestanas.addTab("Historial de Transacciones", crearPanelHistorialTransacciones());
        pestanas.addTab("Transacciones por Cuenta", crearPanelTransaccionesPorCuenta());

        add(panelSuperior, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarSelectorCuentas();
            }
        });
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelNorte = new JPanel(new BorderLayout(10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel("Desde (YYYY-MM-DD):"));
        txtFechaDesdeTotales = new JTextField(10);
        filtros.add(txtFechaDesdeTotales);

        filtros.add(new JLabel("Hasta (YYYY-MM-DD):"));
        txtFechaHastaTotales = new JTextField(10);
        filtros.add(txtFechaHastaTotales);

        JButton btnFiltrar = new JButton("Calcular Totales");
        btnFiltrar.addActionListener(e -> cargarTotales());
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            txtFechaDesdeTotales.setText("");
            txtFechaHastaTotales.setText("");
            cargarTotales();
        });
        filtros.add(btnFiltrar);
        filtros.add(btnLimpiar);

        JPanel panelTarjetas = new JPanel(new GridLayout(1, 3, 12, 12));
        lblTotalDepositado = crearEtiquetaIndicador();
        lblTotalRetirado = crearEtiquetaIndicador();
        lblTotalTransferido = crearEtiquetaIndicador();

        panelTarjetas.add(crearTarjetaIndicador("Total Depositado", lblTotalDepositado));
        panelTarjetas.add(crearTarjetaIndicador("Total Retirado", lblTotalRetirado));
        panelTarjetas.add(crearTarjetaIndicador("Total Transferido", lblTotalTransferido));

        panelNorte.add(filtros, BorderLayout.NORTH);
        panelNorte.add(panelTarjetas, BorderLayout.CENTER);

        modeloTotales = crearModeloTablaNoEditable(
                new Object[]{"Tipo de Operación", "Cantidad de Movimientos", "Monto Total"}
        );
        JTable tabla = new JTable(modeloTotales);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Resumen de Totales por Tipo de Transacción"));

        lblResumenTotales = new JLabel("Total de operaciones: 0 | Monto total operado: $0.00");
        lblResumenTotales.setFont(lblResumenTotales.getFont().deriveFont(Font.BOLD));

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblResumenTotales, BorderLayout.SOUTH);
        return panel;
    }

    private JLabel crearEtiquetaIndicador() {
        JLabel label = new JLabel("$0.00", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        return label;
    }

    private JPanel crearTarjetaIndicador(String titulo, JLabel etiquetaValor) {
        JPanel tarjeta = new JPanel(new BorderLayout(5, 5));
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(12, 10, 12, 10)
        ));
        tarjeta.add(etiquetaValor, BorderLayout.CENTER);
        return tarjeta;
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

    private JPanel crearPanelHistorialTransacciones() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel("Tipo:"));
        cbxTipoHistorial = new JComboBox<>(new String[]{"TODOS", "DEPOSITO", "RETIRO", "TRANSFERENCIA"});
        filtros.add(cbxTipoHistorial);

        filtros.add(new JLabel("Desde (YYYY-MM-DD):"));
        txtFechaDesdeHistorial = new JTextField(10);
        filtros.add(txtFechaDesdeHistorial);

        filtros.add(new JLabel("Hasta (YYYY-MM-DD):"));
        txtFechaHastaHistorial = new JTextField(10);
        filtros.add(txtFechaHastaHistorial);

        JButton btnFiltrar = new JButton("Filtrar");
        btnFiltrar.addActionListener(e -> cargarHistorialTransacciones());
        JButton btnLimpiar = new JButton("Limpiar");
        btnLimpiar.addActionListener(e -> {
            cbxTipoHistorial.setSelectedIndex(0);
            txtFechaDesdeHistorial.setText("");
            txtFechaHastaHistorial.setText("");
            cargarHistorialTransacciones();
        });

        filtros.add(btnFiltrar);
        filtros.add(btnLimpiar);

        modeloHistorial = crearModeloTablaNoEditable(
                new Object[]{"ID", "Fecha y Hora", "Tipo", "Cuenta Origen", "Cuenta Destino", "Monto", "Saldo Resultante"}
        );
        JTable tabla = new JTable(modeloHistorial);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Historial General de Transacciones"));

        lblResumenHistorial = new JLabel("Total de transacciones: 0 | Suma de montos: $0.00");
        lblResumenHistorial.setFont(lblResumenHistorial.getFont().deriveFont(Font.BOLD));

        panel.add(filtros, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblResumenHistorial, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelTransaccionesPorCuenta() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelNorte = new JPanel(new BorderLayout(5, 5));

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtros.add(new JLabel("Cuenta:"));
        cbxCuentaReporte = new JComboBox<>();
        cbxCuentaReporte.setPreferredSize(new Dimension(340, 26));
        filtros.add(cbxCuentaReporte);

        filtros.add(new JLabel("Tipo:"));
        cbxTipoPorCuenta = new JComboBox<>(new String[]{"TODOS", "DEPOSITO", "RETIRO", "TRANSFERENCIA"});
        filtros.add(cbxTipoPorCuenta);

        JButton btnConsultar = new JButton("Consultar");
        btnConsultar.addActionListener(e -> cargarTransaccionesPorCuenta());
        filtros.add(btnConsultar);

        lblInfoCuentaSeleccionada = new JLabel("Seleccione una cuenta para consultar sus transacciones.");
        lblInfoCuentaSeleccionada.setBorder(BorderFactory.createEmptyBorder(0, 10, 4, 10));

        panelNorte.add(filtros, BorderLayout.NORTH);
        panelNorte.add(lblInfoCuentaSeleccionada, BorderLayout.SOUTH);

        modeloPorCuenta = crearModeloTablaNoEditable(
                new Object[]{"ID", "Fecha y Hora", "Tipo", "Cuenta Origen", "Cuenta Destino", "Monto", "Saldo Resultante"}
        );
        JTable tabla = new JTable(modeloPorCuenta);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createTitledBorder("Transacciones de la Cuenta Seleccionada"));

        lblResumenPorCuenta = new JLabel("Movimientos encontrados: 0 | Suma de montos: $0.00");
        lblResumenPorCuenta.setFont(lblResumenPorCuenta.getFont().deriveFont(Font.BOLD));

        panel.add(panelNorte, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(lblResumenPorCuenta, BorderLayout.SOUTH);
        return panel;
    }

    private void cargarTodosLosReportes() {
        logger.info("Actualizando los 8 reportes desde VentanaReportes");
        cargarTotales();
        cargarCuentasActivas();
        cargarCuentasBloqueadas();
        cargarClientesRegistrados();
        cargarHistorialTransacciones();
        cargarSelectorCuentas();
        cargarTransaccionesPorCuenta();
    }

    private void cargarTotales() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = parsearFechaOpcional(txtFechaDesdeTotales.getText(), "Desde");
            hasta = parsearFechaOpcional(txtFechaHastaTotales.getText(), "Hasta");
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de fecha inválido en reporte de totales: {}", ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            logger.warn("Rango de fechas invertido en totales: desde={} > hasta={}", desde, hasta);
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser mayor que la fecha 'Hasta'.",
                    "Rango de Fechas Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTotales.setRowCount(0);
        try {
            ResumenTotales resumen = reporteDAO.obtenerResumenTotales(desde, hasta);

            lblTotalDepositado.setText(formatearMonto(resumen.getTotalDepositado()));
            lblTotalRetirado.setText(formatearMonto(resumen.getTotalRetirado()));
            lblTotalTransferido.setText(formatearMonto(resumen.getTotalTransferido()));

            modeloTotales.addRow(new Object[]{
                    "DEPOSITO", resumen.getCantidadDepositos(), formatearMonto(resumen.getTotalDepositado())
            });
            modeloTotales.addRow(new Object[]{
                    "RETIRO", resumen.getCantidadRetiros(), formatearMonto(resumen.getTotalRetirado())
            });
            modeloTotales.addRow(new Object[]{
                    "TRANSFERENCIA", resumen.getCantidadTransferencias(), formatearMonto(resumen.getTotalTransferido())
            });
            modeloTotales.addRow(new Object[]{
                    "TOTAL GENERAL", resumen.getTotalMovimientos(), formatearMonto(resumen.getMontoTotalOperado())
            });

            lblResumenTotales.setText("Total de operaciones: " + resumen.getTotalMovimientos()
                    + " | Monto total operado: " + formatearMonto(resumen.getMontoTotalOperado()));
        } catch (SQLException ex) {
            logger.error("Error al cargar reporte de totales: {}", ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar reporte de totales", ex);
        }
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

    private void cargarHistorialTransacciones() {
        LocalDate desde;
        LocalDate hasta;
        try {
            desde = parsearFechaOpcional(txtFechaDesdeHistorial.getText(), "Desde");
            hasta = parsearFechaOpcional(txtFechaHastaHistorial.getText(), "Hasta");
        } catch (IllegalArgumentException ex) {
            logger.warn("Filtro de fecha inválido en historial de transacciones: {}", ex.getMessage());
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Fecha Inválida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            logger.warn("Rango de fechas invertido en historial: desde={} > hasta={}", desde, hasta);
            JOptionPane.showMessageDialog(this, "La fecha 'Desde' no puede ser mayor que la fecha 'Hasta'.",
                    "Rango de Fechas Inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloHistorial.setRowCount(0);
        String tipo = (String) cbxTipoHistorial.getSelectedItem();
        try {
            List<ReporteMovimiento> movimientos = reporteDAO.listarHistorialTransacciones(tipo, desde, hasta);
            BigDecimal sumaMontos = BigDecimal.ZERO;
            for (ReporteMovimiento m : movimientos) {
                BigDecimal monto = m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO;
                sumaMontos = sumaMontos.add(monto);
                modeloHistorial.addRow(construirFilaMovimiento(m));
            }
            lblResumenHistorial.setText("Total de transacciones: " + movimientos.size()
                    + " | Suma de montos: " + formatearMonto(sumaMontos));
        } catch (SQLException ex) {
            logger.error("Error al cargar historial de transacciones: {}", ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar historial de transacciones", ex);
        }
    }

    private void cargarSelectorCuentas() {
        CuentaItem anterior = (CuentaItem) cbxCuentaReporte.getSelectedItem();
        try {
            List<CuentaBancaria> cuentas = reporteDAO.listarTodasLasCuentas();
            cbxCuentaReporte.removeAllItems();
            for (CuentaBancaria c : cuentas) {
                CuentaItem item = new CuentaItem(c);
                cbxCuentaReporte.addItem(item);
                if (anterior != null && anterior.getCuenta().getIdCuenta() == c.getIdCuenta()) {
                    cbxCuentaReporte.setSelectedItem(item);
                }
            }
        } catch (SQLException ex) {
            logger.error("Error al cargar selector de cuentas en reportes: {}", ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar listado de cuentas", ex);
        }
    }

    private void cargarTransaccionesPorCuenta() {
        modeloPorCuenta.setRowCount(0);
        CuentaItem seleccionada = (CuentaItem) cbxCuentaReporte.getSelectedItem();
        if (seleccionada == null) {
            lblInfoCuentaSeleccionada.setText("No hay cuentas disponibles para consultar.");
            lblResumenPorCuenta.setText("Movimientos encontrados: 0 | Suma de montos: $0.00");
            return;
        }

        CuentaBancaria cuenta = seleccionada.getCuenta();
        lblInfoCuentaSeleccionada.setText(String.format(
                "Cuenta: %s | Titular: %s | Tipo: %s | Estado: %s | Saldo actual: %s",
                cuenta.getNumeroCuenta(),
                cuenta.getNombreCliente(),
                cuenta.getTipoCuenta(),
                cuenta.getEstado(),
                formatearMonto(cuenta.getSaldo())
        ));

        String tipo = (String) cbxTipoPorCuenta.getSelectedItem();
        try {
            List<ReporteMovimiento> movimientos = reporteDAO.listarTransaccionesPorCuenta(cuenta.getIdCuenta(), tipo);
            BigDecimal sumaMontos = BigDecimal.ZERO;
            for (ReporteMovimiento m : movimientos) {
                BigDecimal monto = m.getMonto() != null ? m.getMonto() : BigDecimal.ZERO;
                sumaMontos = sumaMontos.add(monto);
                modeloPorCuenta.addRow(construirFilaMovimiento(m));
            }
            lblResumenPorCuenta.setText("Movimientos encontrados: " + movimientos.size()
                    + " | Suma de montos: " + formatearMonto(sumaMontos));
        } catch (SQLException ex) {
            logger.error("Error al cargar transacciones por cuenta [idCuenta={}]: {}",
                    cuenta.getIdCuenta(), ex.getMessage(), ex);
            mostrarErrorSql("Error al cargar transacciones de la cuenta", ex);
        }
    }

    private Object[] construirFilaMovimiento(ReporteMovimiento m) {
        return new Object[]{
                m.getId(),
                formatearFechaHora(m.getFecha()),
                m.getTipo(),
                formatearCuenta(m.getNumeroCuentaOrigen(), m.getCuentaOrigenId()),
                formatearCuenta(m.getNumeroCuentaDestino(), m.getCuentaDestinoId()),
                formatearMonto(m.getMonto()),
                formatearMonto(m.getSaldoResultante())
        };
    }

    private String formatearCuenta(String numeroCuenta, Integer idCuenta) {
        if (numeroCuenta != null && !numeroCuenta.trim().isEmpty()) {
            return numeroCuenta;
        }
        return idCuenta != null ? "ID #" + idCuenta : "N/A";
    }

    private String formatearFechaHora(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA_HORA) : "";
    }

    public static LocalDate parsearFechaOpcional(String texto, String nombreCampo) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "El campo '" + nombreCampo + "' debe tener formato YYYY-MM-DD (ejemplo: 2026-11-21).");
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

    private static class CuentaItem {
        private final CuentaBancaria cuenta;

        public CuentaItem(CuentaBancaria cuenta) {
            this.cuenta = cuenta;
        }

        public CuentaBancaria getCuenta() {
            return cuenta;
        }

        @Override
        public String toString() {
            return cuenta.getNumeroCuenta() + " - " + cuenta.getNombreCliente() + " [" + cuenta.getEstado() + "]";
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaReportes().setVisible(true));
    }
}
