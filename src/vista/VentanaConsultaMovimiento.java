package vista;

import excepciones.MontoInvalidoException;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.TransaccionService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class VentanaConsultaMovimiento extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaConsultaMovimiento.class);
    private final TransaccionService transaccionService;

    private JTextField txtIdMovimiento;
    private JButton btnBuscar;
    private JButton btnLimpiar;

    private JLabel lblId;
    private JLabel lblTipo;
    private JLabel lblFecha;
    private JLabel lblCuentaOrigen;
    private JLabel lblCuentaDestino;
    private JLabel lblMonto;
    private JLabel lblSaldoResultante;

    public VentanaConsultaMovimiento() {
        this.transaccionService = new TransaccionService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Consulta Individual de Movimiento");
        setSize(500, 390);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Panel Norte: Buscador por ID
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBusqueda.setBorder(BorderFactory.createTitledBorder("Buscar Transacción"));
        panelBusqueda.add(new JLabel("ID de Movimiento:"));
        txtIdMovimiento = new JTextField(10);
        btnBuscar = new JButton("Consultar");
        btnLimpiar = new JButton("Limpiar");

        panelBusqueda.add(txtIdMovimiento);
        panelBusqueda.add(btnBuscar);
        panelBusqueda.add(btnLimpiar);

        // Panel Centro: Detalle tipo comprobante
        JPanel panelDetalle = new JPanel(new GridLayout(7, 2, 10, 8));
        panelDetalle.setBorder(BorderFactory.createTitledBorder("Detalle del Comprobante"));

        lblId = new JLabel("---");
        lblTipo = new JLabel("---");
        lblFecha = new JLabel("---");
        lblCuentaOrigen = new JLabel("---");
        lblCuentaDestino = new JLabel("---");
        lblMonto = new JLabel("---");
        lblSaldoResultante = new JLabel("---");

        Font fontValor = lblId.getFont().deriveFont(Font.BOLD);
        lblId.setFont(fontValor);
        lblTipo.setFont(fontValor);
        lblFecha.setFont(fontValor);
        lblCuentaOrigen.setFont(fontValor);
        lblCuentaDestino.setFont(fontValor);
        lblMonto.setFont(fontValor);
        lblSaldoResultante.setFont(fontValor);

        panelDetalle.add(new JLabel("  ID Movimiento:"));
        panelDetalle.add(lblId);
        panelDetalle.add(new JLabel("  Tipo de Operación:"));
        panelDetalle.add(lblTipo);
        panelDetalle.add(new JLabel("  Fecha y Hora:"));
        panelDetalle.add(lblFecha);
        panelDetalle.add(new JLabel("  Cuenta Origen:"));
        panelDetalle.add(lblCuentaOrigen);
        panelDetalle.add(new JLabel("  Cuenta Destino:"));
        panelDetalle.add(lblCuentaDestino);
        panelDetalle.add(new JLabel("  Monto Operado:"));
        panelDetalle.add(lblMonto);
        panelDetalle.add(new JLabel("  Saldo Resultante:"));
        panelDetalle.add(lblSaldoResultante);

        add(panelBusqueda, BorderLayout.NORTH);
        add(panelDetalle, BorderLayout.CENTER);

        btnBuscar.addActionListener(e -> buscarMovimiento());
        btnLimpiar.addActionListener(e -> limpiarCampos());
    }

    private void buscarMovimiento() {
        String idTexto = txtIdMovimiento.getText().trim();
        if (idTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el ID del movimiento a consultar.", "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idTexto);
        } catch (NumberFormatException ex) {
            logger.warn("ID de movimiento no numérico ingresado: '{}'", idTexto);
            JOptionPane.showMessageDialog(this, "El ID del movimiento debe ser un número entero.", "Formato inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Movimiento mov = transaccionService.consultarMovimientoPorId(id);
            if (mov == null) {
                limpiarEtiquetas();
                JOptionPane.showMessageDialog(this, "No existe ningún movimiento con el ID: " + id, "Sin resultados", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            lblId.setText(String.valueOf(mov.getId()));
            lblTipo.setText(mov.getTipo());
            lblFecha.setText(mov.getFecha() != null ? mov.getFecha().format(formatter) : "N/A");
            lblCuentaOrigen.setText(transaccionService.obtenerNumeroCuentaPorId(mov.getCuentaOrigenId()));
            lblCuentaDestino.setText(transaccionService.obtenerNumeroCuentaPorId(mov.getCuentaDestinoId()));
            lblMonto.setText(String.format("$%.2f", mov.getMonto()));
            lblSaldoResultante.setText(String.format("$%.2f", mov.getSaldoResultante()));

        } catch (MontoInvalidoException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Validación", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            logger.error("Error SQL al consultar movimiento ID {}: {}", id, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarEtiquetas() {
        lblId.setText("---");
        lblTipo.setText("---");
        lblFecha.setText("---");
        lblCuentaOrigen.setText("---");
        lblCuentaDestino.setText("---");
        lblMonto.setText("---");
        lblSaldoResultante.setText("---");
    }

    private void limpiarCampos() {
        txtIdMovimiento.setText("");
        limpiarEtiquetas();
        txtIdMovimiento.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaConsultaMovimiento().setVisible(true));
    }
}