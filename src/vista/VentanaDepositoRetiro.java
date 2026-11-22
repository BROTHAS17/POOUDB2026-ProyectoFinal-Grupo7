package vista;

import excepciones.CuentaInactivaException;
import excepciones.CuentaNoEncontradaException;
import excepciones.MontoInvalidoException;
import excepciones.SaldoInsuficienteException;
import modelo.CuentaBancaria;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.TransaccionService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class VentanaDepositoRetiro extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaDepositoRetiro.class);
    private final TransaccionService transaccionService;

    private JRadioButton rbDeposito;
    private JRadioButton rbRetiro;
    private JTextField txtNumeroCuenta;
    private JButton btnVerificarCuenta;
    private JLabel lblTitular;
    private JLabel lblEstadoCuenta;
    private JLabel lblSaldoActual;
    private JTextField txtMonto;
    private JButton btnProcesar;
    private JButton btnLimpiar;

    public VentanaDepositoRetiro() {
        this.transaccionService = new TransaccionService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Depósitos y Retiros");
        setSize(520, 410);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Panel Superior: Selección de Tipo de Operación
        JPanel panelTipo = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelTipo.setBorder(BorderFactory.createTitledBorder("1. Seleccione el Tipo de Operación"));
        rbDeposito = new JRadioButton("Depósito", true);
        rbRetiro = new JRadioButton("Retiro");
        rbDeposito.setFont(rbDeposito.getFont().deriveFont(Font.BOLD, 13f));
        rbRetiro.setFont(rbRetiro.getFont().deriveFont(Font.BOLD, 13f));

        ButtonGroup grupoOperacion = new ButtonGroup();
        grupoOperacion.add(rbDeposito);
        grupoOperacion.add(rbRetiro);

        panelTipo.add(rbDeposito);
        panelTipo.add(rbRetiro);

        // Panel Central: Datos de la Cuenta y Monto
        JPanel panelCentro = new JPanel(new GridBagLayout());
        panelCentro.setBorder(BorderFactory.createTitledBorder("2. Datos de la Transacción"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: Número de cuenta + botón verificar
        gbc.gridx = 0; gbc.gridy = 0;
        panelCentro.add(new JLabel("Número de Cuenta:"), gbc);

        txtNumeroCuenta = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 0;
        panelCentro.add(txtNumeroCuenta, gbc);

        btnVerificarCuenta = new JButton("Verificar");
        gbc.gridx = 2; gbc.gridy = 0;
        panelCentro.add(btnVerificarCuenta, gbc);

        // Fila 1: Titular
        gbc.gridx = 0; gbc.gridy = 1;
        panelCentro.add(new JLabel("Titular:"), gbc);
        lblTitular = new JLabel("---");
        lblTitular.setFont(lblTitular.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1; gbc.gridy = 1; gbc.gridwidth = 2;
        panelCentro.add(lblTitular, gbc);

        // Fila 2: Estado
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        panelCentro.add(new JLabel("Estado de Cuenta:"), gbc);
        lblEstadoCuenta = new JLabel("---");
        lblEstadoCuenta.setFont(lblEstadoCuenta.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 2;
        panelCentro.add(lblEstadoCuenta, gbc);

        // Fila 3: Saldo Actual
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panelCentro.add(new JLabel("Saldo Actual:"), gbc);
        lblSaldoActual = new JLabel("$0.00");
        lblSaldoActual.setFont(lblSaldoActual.getFont().deriveFont(Font.BOLD, 14f));
        lblSaldoActual.setForeground(new Color(0, 102, 51));
        gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 2;
        panelCentro.add(lblSaldoActual, gbc);

        // Fila 4: Monto a operar
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 1;
        panelCentro.add(new JLabel("Monto ($):"), gbc);
        txtMonto = new JTextField(15);
        gbc.gridx = 1; gbc.gridy = 4; gbc.gridwidth = 2;
        panelCentro.add(txtMonto, gbc);

        // Panel Inferior: Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnProcesar = new JButton("Procesar Operación");
        btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnProcesar);
        panelBotones.add(btnLimpiar);

        add(panelTipo, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        btnVerificarCuenta.addActionListener(e -> verificarCuenta());
        btnProcesar.addActionListener(e -> procesarOperacion());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void verificarCuenta() {
        String numCuenta = txtNumeroCuenta.getText().trim();
        try {
            CuentaBancaria cuenta = transaccionService.buscarCuentaPorNumero(numCuenta);
            lblTitular.setText(cuenta.getNombreCliente());
            lblEstadoCuenta.setText(cuenta.getEstado());
            lblEstadoCuenta.setForeground("ACTIVA".equalsIgnoreCase(cuenta.getEstado())
                    ? new Color(0, 128, 0) : Color.RED);
            lblSaldoActual.setText(String.format("$%.2f", cuenta.getSaldo()));
        } catch (CuentaNoEncontradaException ex) {
            resetearInfoCuenta();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cuenta no encontrada", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            logger.error("Error SQL al verificar cuenta: {}", ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void procesarOperacion() {
        String numCuenta = txtNumeroCuenta.getText().trim();
        String montoTexto = txtMonto.getText().trim();

        if (numCuenta.isEmpty() || montoTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete el número de cuenta y el monto.", "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(montoTexto);
        } catch (NumberFormatException ex) {
            logger.warn("Formato numérico inválido ingresado en monto: '{}'", montoTexto);
            JOptionPane.showMessageDialog(this, "Ingrese un valor numérico válido para el monto (ej. 50.00).", "Formato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Movimiento mov;
            String tipoOperacion;
            if (rbDeposito.isSelected()) {
                mov = transaccionService.realizarDeposito(numCuenta, monto);
                tipoOperacion = "Depósito";
            } else {
                mov = transaccionService.realizarRetiro(numCuenta, monto);
                tipoOperacion = "Retiro";
            }

            // Actualizar etiqueta de saldo en pantalla
            lblSaldoActual.setText(String.format("$%.2f", mov.getSaldoResultante()));
            txtMonto.setText("");

            JOptionPane.showMessageDialog(this,
                    String.format("%s realizado exitosamente.\nID de Movimiento: %d\nMonto: $%.2f\nNuevo Saldo: $%.2f",
                            tipoOperacion, mov.getId(), mov.getMonto(), mov.getSaldoResultante()),
                    "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);

            // Refrescar datos de la cuenta
            verificarCuenta();

        } catch (MontoInvalidoException | CuentaNoEncontradaException | CuentaInactivaException | SaldoInsuficienteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Operación Rechazada", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error técnico en base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetearInfoCuenta() {
        lblTitular.setText("---");
        lblEstadoCuenta.setText("---");
        lblEstadoCuenta.setForeground(Color.BLACK);
        lblSaldoActual.setText("$0.00");
    }

    private void limpiarFormulario() {
        txtNumeroCuenta.setText("");
        txtMonto.setText("");
        resetearInfoCuenta();
        txtNumeroCuenta.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaDepositoRetiro().setVisible(true));
    }
}