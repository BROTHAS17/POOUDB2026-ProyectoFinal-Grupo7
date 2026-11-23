package vista;

import excepciones.CuentaNoEncontradaException;
import modelo.CuentaBancaria;
import modelo.Movimiento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import service.TransaccionService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class VentanaHistorial extends JFrame {

    private static final Logger logger = LogManager.getLogger(VentanaHistorial.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final String[] COLUMNAS = {
            "ID", "Fecha y hora", "Operación", "Cuenta relacionada", "Monto", "Saldo después"
    };

    private final TransaccionService transaccionService;

    private JTextField txtNumeroCuenta;
    private JButton btnMostrar;
    private JButton btnLimpiar;
    private JLabel lblTitular;
    private JLabel lblEstado;
    private JLabel lblSaldo;
    private JLabel lblResumen;
    private DefaultTableModel modeloTabla;
    private JTable tablaMovimientos;

    public VentanaHistorial() {
        this.transaccionService = new TransaccionService();
        initComponents();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Historial de Movimientos");
        setSize(860, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // norte, búsqueda y datos de la cuenta
        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        panelBusqueda.add(new JLabel("Número de Cuenta:"));
        txtNumeroCuenta = new JTextField(14);
        btnMostrar = new JButton("Mostrar historial");
        btnLimpiar = new JButton("Limpiar");
        panelBusqueda.add(txtNumeroCuenta);
        panelBusqueda.add(btnMostrar);
        panelBusqueda.add(btnLimpiar);

        JPanel panelDatos = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 4));
        lblTitular = crearEtiquetaValor();
        lblEstado = crearEtiquetaValor();
        lblSaldo = crearEtiquetaValor();
        lblSaldo.setForeground(new Color(0, 102, 51));
        panelDatos.add(new JLabel("Titular:"));
        panelDatos.add(lblTitular);
        panelDatos.add(new JLabel("Estado:"));
        panelDatos.add(lblEstado);
        panelDatos.add(new JLabel("Saldo actual:"));
        panelDatos.add(lblSaldo);

        JPanel panelNorte = new JPanel(new GridLayout(2, 1));
        panelNorte.setBorder(BorderFactory.createTitledBorder("Cuenta"));
        panelNorte.add(panelBusqueda);
        panelNorte.add(panelDatos);

        // centro, tabla de movimientos de solo lectura
        modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaMovimientos = new JTable(modeloTabla);
        tablaMovimientos.setRowHeight(22);
        tablaMovimientos.getTableHeader().setReorderingAllowed(false);
        tablaMovimientos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer derecha = new DefaultTableCellRenderer();
        derecha.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaMovimientos.getColumnModel().getColumn(4).setCellRenderer(derecha);
        tablaMovimientos.getColumnModel().getColumn(5).setCellRenderer(derecha);

        int[] anchos = {50, 150, 170, 150, 110, 110};
        for (int i = 0; i < anchos.length; i++) {
            tablaMovimientos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tablaMovimientos);
        scroll.setBorder(BorderFactory.createTitledBorder("Movimientos (del más reciente al más antiguo)"));

        // sur, resumen
        lblResumen = new JLabel(" ");
        lblResumen.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));

        add(panelNorte, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(lblResumen, BorderLayout.SOUTH);

        btnMostrar.addActionListener(e -> mostrarHistorial());
        txtNumeroCuenta.addActionListener(e -> mostrarHistorial());
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private JLabel crearEtiquetaValor() {
        JLabel lbl = new JLabel("---");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        return lbl;
    }

    private void mostrarHistorial() {
        String numero = txtNumeroCuenta.getText().trim();
        if (numero.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el número de cuenta.", "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            CuentaBancaria cuenta = transaccionService.buscarCuentaPorNumero(numero);
            List<Movimiento> movimientos = transaccionService.obtenerHistorial(cuenta);
            Map<Integer, String> numeros = transaccionService.obtenerNumerosDeCuenta();

            lblTitular.setText(cuenta.getNombreCliente());
            lblEstado.setText(cuenta.getEstado());
            lblEstado.setForeground("ACTIVA".equalsIgnoreCase(cuenta.getEstado())
                    ? new Color(0, 128, 0) : Color.RED);
            lblSaldo.setText(String.format("$%.2f", cuenta.getSaldo()));

            modeloTabla.setRowCount(0);
            boolean hayTransferenciaRecibida = false;
            for (Movimiento mov : movimientos) {
                Object[] fila = crearFila(mov, cuenta.getIdCuenta(), numeros);
                modeloTabla.addRow(fila);
                if ("Transferencia recibida".equals(fila[2])) {
                    hayTransferenciaRecibida = true;
                }
            }

            if (movimientos.isEmpty()) {
                lblResumen.setText("La cuenta " + cuenta.getNumeroCuenta() + " no tiene movimientos registrados.");
            } else {
                String texto = movimientos.size() + (movimientos.size() == 1 ? " movimiento." : " movimientos.");
                if (hayTransferenciaRecibida) {
                    texto += "  En las transferencias recibidas no se muestra el saldo después, "
                            + "porque la base solo guarda el saldo de la cuenta de origen.";
                }
                lblResumen.setText(texto);
            }

        } catch (CuentaNoEncontradaException ex) {
            limpiarResultados();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cuenta no encontrada", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            logger.error("Error SQL al mostrar historial de la cuenta {}: {}", numero, ex.getMessage(), ex);
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * arma una fila de la tabla desde el punto de vista de la cuenta IDCUENTA,
     * en una transferencia indica si se envió o se recibió y cuál fue la otra cuenta,
     * en una transferencia recibida el saldo después se muestra como --- porque SALDO_RESULTANTE
     * guarda el saldo de la cuenta de origen
     */
    private Object[] crearFila(Movimiento mov, int idCuenta, Map<Integer, String> numeros) {
        String operacion;
        String relacionada = "---";
        String signo;
        String saldoDespues = String.format("$%.2f", mov.getSaldoResultante());

        switch (mov.getTipo()) {
            case "DEPOSITO":
                operacion = "Depósito";
                signo = "+";
                break;
            case "RETIRO":
                operacion = "Retiro";
                signo = "-";
                break;
            case "TRANSFERENCIA":
                boolean enviada = mov.getCuentaOrigenId() != null && mov.getCuentaOrigenId() == idCuenta;
                if (enviada) {
                    operacion = "Transferencia enviada";
                    relacionada = "A " + numeroDe(mov.getCuentaDestinoId(), numeros);
                    signo = "-";
                } else {
                    operacion = "Transferencia recibida";
                    relacionada = "De " + numeroDe(mov.getCuentaOrigenId(), numeros);
                    signo = "+";
                    saldoDespues = "---";
                }
                break;
            default:
                operacion = mov.getTipo();
                signo = "";
        }

        return new Object[]{
                mov.getId(),
                mov.getFecha() != null ? mov.getFecha().format(FORMATO_FECHA) : "N/A",
                operacion,
                relacionada,
                String.format("%s$%.2f", signo, mov.getMonto()),
                saldoDespues
        };
    }

    private String numeroDe(Integer idCuenta, Map<Integer, String> numeros) {
        if (idCuenta == null) {
            return "N/A";
        }
        return numeros.getOrDefault(idCuenta, "ID " + idCuenta);
    }

    private void limpiarResultados() {
        lblTitular.setText("---");
        lblEstado.setText("---");
        lblEstado.setForeground(Color.BLACK);
        lblSaldo.setText("---");
        modeloTabla.setRowCount(0);
        lblResumen.setText(" ");
    }

    private void limpiar() {
        txtNumeroCuenta.setText("");
        limpiarResultados();
        txtNumeroCuenta.requestFocus();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaHistorial().setVisible(true));
    }
}
