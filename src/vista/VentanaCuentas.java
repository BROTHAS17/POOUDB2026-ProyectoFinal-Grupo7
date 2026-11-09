package vista;

import dao.ClienteDAO;
import dao.CuentaDAO;
import modelo.Cliente;
import modelo.CuentaBancaria;
import validacion.ValidadorCuenta;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class VentanaCuentas extends JFrame {
    private JTextField txtNumeroCuenta;
    private JComboBox<String> cbxTipoCuenta;
    private JComboBox<ClienteItem> cbxCliente;
    private JButton btnRegistrar;
    private JButton btnListar;
    private JTable tablaCuentas;
    private DefaultTableModel modeloTabla;

    private final CuentaDAO cuentaDAO;
    private final ClienteDAO clienteDAO;

    public VentanaCuentas() {
        this.cuentaDAO = new CuentaDAO();
        this.clienteDAO = new ClienteDAO();
        initComponents();
        cargarClientes();
        cargarDatosTabla();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Registro de Cuentas");
        setSize(850, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos de la Cuenta"));

        txtNumeroCuenta = new JTextField();
        cbxTipoCuenta = new JComboBox<>(new String[]{"AHORRO", "CORRIENTE"});
        cbxCliente = new JComboBox<>();

        panelFormulario.add(new JLabel(" Número de Cuenta:"));
        panelFormulario.add(txtNumeroCuenta);
        panelFormulario.add(new JLabel(" Tipo de Cuenta:"));
        panelFormulario.add(cbxTipoCuenta);
        panelFormulario.add(new JLabel(" Cliente:"));
        panelFormulario.add(cbxCliente);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegistrar = new JButton("Registrar Cuenta");
        btnListar = new JButton("Actualizar Lista");

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnListar);

        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);

        modeloTabla = new DefaultTableModel(
                new Object[]{"ID", "Número", "Tipo", "Fecha Apertura", "Saldo", "Estado", "Cliente"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCuentas = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaCuentas);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Cuentas Registradas"));

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        btnRegistrar.addActionListener(e -> registrarCuenta());
        btnListar.addActionListener(e -> {
            cargarClientes();
            cargarDatosTabla();
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowActivated(WindowEvent e) {
                cargarClientes();
            }
        });
    }

    private void cargarClientes() {
        ClienteItem anterior = (ClienteItem) cbxCliente.getSelectedItem();
        try {
            List<Cliente> clientes = clienteDAO.listar();
            cbxCliente.removeAllItems();
            boolean anteriorSigue = false;
            for (Cliente c : clientes) {
                if ("ACTIVO".equalsIgnoreCase(c.getEstado())) {
                    ClienteItem item = new ClienteItem(c.getIdCliente(), c.getNombreCompleto());
                    cbxCliente.addItem(item);
                    if (anterior != null && anterior.getId() == item.getId()) {
                        cbxCliente.setSelectedItem(item);
                        anteriorSigue = true;
                    }
                }
            }
            if (anterior != null && !anteriorSigue) {
                cbxCliente.setSelectedIndex(-1);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<CuentaBancaria> cuentas = cuentaDAO.listar();
            for (CuentaBancaria c : cuentas) {
                modeloTabla.addRow(new Object[]{
                        c.getIdCuenta(),
                        c.getNumeroCuenta(),
                        c.getTipoCuenta(),
                        c.getFechaApertura(),
                        "$" + c.getSaldo(),
                        c.getEstado(),
                        c.getNombreCliente()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar cuentas: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void registrarCuenta() {
        CuentaBancaria c = new CuentaBancaria();
        c.setNumeroCuenta(txtNumeroCuenta.getText());
        c.setTipoCuenta((String) cbxTipoCuenta.getSelectedItem());
        c.setFechaApertura(Date.valueOf(LocalDate.now()));
        c.setSaldo(new BigDecimal("0.00"));
        c.setEstado("ACTIVA");

        ClienteItem clienteSeleccionado = (ClienteItem) cbxCliente.getSelectedItem();
        if (clienteSeleccionado != null) {
            c.setIdCliente(clienteSeleccionado.getId());
        } else {
            c.setIdCliente(0);
        }

        List<String> errores = ValidadorCuenta.validar(c);
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n", errores), "Errores de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // El cliente pudo cambiar de estado mientras esta ventana estaba abierta.
            boolean clienteActivo = false;
            for (Cliente cl : clienteDAO.listar()) {
                if (cl.getIdCliente() == c.getIdCliente() && "ACTIVO".equalsIgnoreCase(cl.getEstado())) {
                    clienteActivo = true;
                    break;
                }
            }
            if (!clienteActivo) {
                JOptionPane.showMessageDialog(this,
                        "El cliente seleccionado ya no está ACTIVO. Solo se pueden crear cuentas para clientes activos.",
                        "Cliente no activo", JOptionPane.WARNING_MESSAGE);
                cargarClientes();
                return;
            }

            if (cuentaDAO.existeNumero(c.getNumeroCuenta())) {
                JOptionPane.showMessageDialog(this, "El número de cuenta ya existe en la base de datos.", "Número Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (cuentaDAO.registrar(c)) {
                JOptionPane.showMessageDialog(this, "Cuenta registrada exitosamente.");
                txtNumeroCuenta.setText("");
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar la cuenta.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ClienteItem {
        private final int id;
        private final String nombre;

        public ClienteItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() { return id; }

        @Override
        public String toString() { return nombre; }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VentanaCuentas().setVisible(true));
    }
}