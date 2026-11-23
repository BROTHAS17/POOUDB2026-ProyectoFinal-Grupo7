package vista;

import excepciones.CuentaInactivaException;
import excepciones.CuentaNoEncontradaException;
import excepciones.MontoInvalidoException;
import excepciones.OperacionNoPermitidaException;
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

public class VentanaTransferencia extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaTransferencia.class);
    private final TransaccionService transaccionService;

    private PanelCuenta panelOrigen;
    private PanelCuenta panelDestino;
    private JTextField txtMonto;
    private JButton btnTransferir;
    private JButton btnLimpiar;

    public VentanaTransferencia() {
        this.transaccionService = new TransaccionService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Transferencias");
        setSize(720, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // centro, las dos cuentas una al lado de la otra
        panelOrigen = new PanelCuenta("1. Cuenta de origen (se descuenta)");
        panelDestino = new PanelCuenta("2. Cuenta de destino (se abona)");

        JPanel panelCuentas = new JPanel(new GridLayout(1, 2, 10, 0));
        panelCuentas.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        panelCuentas.add(panelOrigen);
        panelCuentas.add(panelDestino);

        // sur, monto y botones
        JPanel panelMonto = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 10));
        panelMonto.setBorder(BorderFactory.createTitledBorder("3. Monto a transferir"));
        panelMonto.add(new JLabel("Monto ($):"));
        txtMonto = new JTextField(12);
        panelMonto.add(txtMonto);

        btnTransferir = new JButton("Transferir");
        btnTransferir.setFont(btnTransferir.getFont().deriveFont(Font.BOLD));
        btnLimpiar = new JButton("Limpiar");
        panelMonto.add(btnTransferir);
        panelMonto.add(btnLimpiar);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelSur.add(panelMonto, BorderLayout.CENTER);

        add(panelCuentas, BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);

        btnTransferir.addActionListener(e -> procesarTransferencia());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
    }

    private void procesarTransferencia() {
        String origen = panelOrigen.getNumeroCuenta();
        String destino = panelDestino.getNumeroCuenta();
        String montoTexto = txtMonto.getText().trim();

        if (origen.isEmpty() || destino.isEmpty() || montoTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Complete la cuenta de origen, la cuenta de destino y el monto.",
                    "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal monto;
        try {
            monto = new BigDecimal(montoTexto);
        } catch (NumberFormatException ex) {
            logger.warn("Formato numérico inválido ingresado en monto de transferencia: '{}'", montoTexto);
            JOptionPane.showMessageDialog(this,
                    "Ingrese un valor numérico válido para el monto (ej. 25.50).",
                    "Formato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // se revisan las reglas que no necesitan la base antes de preguntar, para no pedir confirmación de un monto inválido
        try {
            transaccionService.validarDatosTransferencia(origen, destino, monto);
        } catch (CuentaNoEncontradaException | MontoInvalidoException | OperacionNoPermitidaException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transferencia Rechazada", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(this,
                String.format("¿Confirma transferir $%.2f de la cuenta %s a la cuenta %s?", monto, origen, destino),
                "Confirmar transferencia", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            Movimiento mov = transaccionService.realizarTransferencia(origen, destino, monto);
            txtMonto.setText("");

            JOptionPane.showMessageDialog(this,
                    String.format("Transferencia realizada exitosamente.%nID de Movimiento: %d%nMonto: $%.2f%n"
                                    + "De: %s%nA: %s%nNuevo saldo de la cuenta de origen: $%.2f",
                            mov.getId(), mov.getMonto(), origen, destino, mov.getSaldoResultante()),
                    "Operación Exitosa", JOptionPane.INFORMATION_MESSAGE);

            // refrescar los datos de ambas cuentas con lo que quedó en la base
            panelOrigen.verificar();
            panelDestino.verificar();

        } catch (MontoInvalidoException | OperacionNoPermitidaException | CuentaNoEncontradaException
                 | CuentaInactivaException | SaldoInsuficienteException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Transferencia Rechazada", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error técnico en base de datos. La transferencia no se realizó y ningún saldo cambió.\n" + ex.getMessage(),
                    "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        panelOrigen.limpiar();
        panelDestino.limpiar();
        txtMonto.setText("");
        panelOrigen.enfocar();
    }

    /**
     * panel con el número de una cuenta, el botón verificar y los datos de la cuenta,
     * se usa dos veces, para la cuenta de origen y para la de destino
     */
    private class PanelCuenta extends JPanel {

        private final JTextField txtNumero;
        private final JLabel lblTitular;
        private final JLabel lblEstado;
        private final JLabel lblSaldo;

        PanelCuenta(String titulo) {
            super(new GridBagLayout());
            setBorder(BorderFactory.createTitledBorder(titulo));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(6, 8, 6, 8);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0; gbc.gridy = 0;
            add(new JLabel("Número de Cuenta:"), gbc);
            txtNumero = new JTextField(10);
            gbc.gridx = 1; gbc.weightx = 1;
            add(txtNumero, gbc);
            JButton btnVerificar = new JButton("Verificar");
            gbc.gridx = 2; gbc.weightx = 0;
            add(btnVerificar, gbc);

            gbc.gridx = 0; gbc.gridy = 1;
            add(new JLabel("Titular:"), gbc);
            lblTitular = crearEtiquetaValor();
            gbc.gridx = 1; gbc.gridwidth = 2;
            add(lblTitular, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
            add(new JLabel("Estado:"), gbc);
            lblEstado = crearEtiquetaValor();
            gbc.gridx = 1; gbc.gridwidth = 2;
            add(lblEstado, gbc);

            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
            add(new JLabel("Saldo Actual:"), gbc);
            lblSaldo = crearEtiquetaValor();
            lblSaldo.setFont(lblSaldo.getFont().deriveFont(Font.BOLD, 14f));
            lblSaldo.setForeground(new Color(0, 102, 51));
            gbc.gridx = 1; gbc.gridwidth = 2;
            add(lblSaldo, gbc);

            btnVerificar.addActionListener(e -> verificar());
            txtNumero.addActionListener(e -> verificar());
        }

        private JLabel crearEtiquetaValor() {
            JLabel lbl = new JLabel("---");
            lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
            return lbl;
        }

        String getNumeroCuenta() {
            return txtNumero.getText().trim();
        }

        void verificar() {
            String numero = getNumeroCuenta();
            try {
                CuentaBancaria cuenta = transaccionService.buscarCuentaPorNumero(numero);
                lblTitular.setText(cuenta.getNombreCliente());
                lblEstado.setText(cuenta.getEstado());
                lblEstado.setForeground("ACTIVA".equalsIgnoreCase(cuenta.getEstado())
                        ? new Color(0, 128, 0) : Color.RED);
                lblSaldo.setText(String.format("$%.2f", cuenta.getSaldo()));
            } catch (CuentaNoEncontradaException ex) {
                limpiarDatos();
                JOptionPane.showMessageDialog(VentanaTransferencia.this, ex.getMessage(),
                        "Cuenta no encontrada", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                logger.error("Error SQL al verificar cuenta {}: {}", numero, ex.getMessage(), ex);
                JOptionPane.showMessageDialog(VentanaTransferencia.this,
                        "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void limpiarDatos() {
            lblTitular.setText("---");
            lblEstado.setText("---");
            lblEstado.setForeground(Color.BLACK);
            lblSaldo.setText("---");
        }

        void limpiar() {
            txtNumero.setText("");
            limpiarDatos();
        }

        void enfocar() {
            txtNumero.requestFocus();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaTransferencia().setVisible(true));
    }
}
