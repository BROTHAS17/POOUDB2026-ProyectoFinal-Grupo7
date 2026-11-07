package vista;

import dao.ClienteDAO;
import modelo.Cliente;
import validacion.ValidadorCliente;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz de usuario Swing para administrar la tabla clientes.
 */
public class VentanaClientes extends JFrame {

    private JTextField txtIdCliente;
    private JTextField txtCodigo;
    private JTextField txtDui;
    private JTextField txtNombre;
    private JTextField txtTelefono;
    private JTextField txtCorreo;
    private JTextField txtDireccion;
    private JComboBox<String> cbxEstado;

    private JButton btnRegistrar;
    private JButton btnListar;
    private JButton btnModificar;
    private JButton btnCambiarEstado;

    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;

    private final ClienteDAO clienteDAO;

    public VentanaClientes() {
        this.clienteDAO = new ClienteDAO();
        initComponents();
        cargarDatosTabla();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Administración de Clientes");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel de Formulario
        JPanel panelFormulario = new JPanel(new GridLayout(8, 2, 5, 5));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));

        txtIdCliente = new JTextField();
        txtIdCliente.setEditable(false);

        txtCodigo = new JTextField();
        txtDui = new JTextField();
        txtNombre = new JTextField();
        txtTelefono = new JTextField();
        txtCorreo = new JTextField();
        txtDireccion = new JTextField();
        cbxEstado = new JComboBox<>(new String[]{"ACTIVO", "INACTIVO", "BLOQUEADO"});

        panelFormulario.add(new JLabel(" ID Cliente:"));
        panelFormulario.add(txtIdCliente);
        panelFormulario.add(new JLabel(" Código Cliente:"));
        panelFormulario.add(txtCodigo);
        panelFormulario.add(new JLabel(" DUI (00000000-0):"));
        panelFormulario.add(txtDui);
        panelFormulario.add(new JLabel(" Nombre Completo:"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel(" Teléfono:"));
        panelFormulario.add(txtTelefono);
        panelFormulario.add(new JLabel(" Correo Electrónico:"));
        panelFormulario.add(txtCorreo);
        panelFormulario.add(new JLabel(" Dirección:"));
        panelFormulario.add(txtDireccion);
        panelFormulario.add(new JLabel(" Estado:"));
        panelFormulario.add(cbxEstado);

        // Panel de Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnRegistrar = new JButton("Registrar");
        btnListar = new JButton("Listar");
        btnModificar = new JButton("Modificar");
        btnCambiarEstado = new JButton("Cambiar estado");

        panelBotones.add(btnRegistrar);
        panelBotones.add(btnListar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnCambiarEstado);

        // Panel Superior
        JPanel panelNorte = new JPanel(new BorderLayout());
        panelNorte.add(panelFormulario, BorderLayout.CENTER);
        panelNorte.add(panelBotones, BorderLayout.SOUTH);

        // Tabla
        modeloTabla = new DefaultTableModel(
            new Object[]{"ID", "Código", "DUI", "Nombre", "Teléfono", "Correo", "Dirección", "Estado"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaClientes = new JTable(modeloTabla);
        JScrollPane scrollTabla = new JScrollPane(tablaClientes);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Lista de Clientes (Datos Persona 1)"));

        add(panelNorte, BorderLayout.NORTH);
        add(scrollTabla, BorderLayout.CENTER);

        // Eventos
        btnRegistrar.addActionListener(e -> registrarCliente());
        btnListar.addActionListener(e -> cargarDatosTabla());
        btnModificar.addActionListener(e -> modificarCliente());
        btnCambiarEstado.addActionListener(e -> cambiarEstadoCliente());

        tablaClientes.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                seleccionarFilaTabla();
            }
        });
    }

    private Cliente construirClienteFormulario() {
        Cliente c = new Cliente();
        if (!txtIdCliente.getText().trim().isEmpty()) {
            c.setIdCliente(Integer.parseInt(txtIdCliente.getText().trim()));
        } else {
            c.setIdCliente(0);
        }
        c.setCodigoCliente(txtCodigo.getText());
        c.setDui(txtDui.getText());
        c.setNombreCompleto(txtNombre.getText());
        c.setTelefono(txtTelefono.getText());
        c.setCorreo(txtCorreo.getText());
        c.setDireccion(txtDireccion.getText());
        c.setEstado((String) cbxEstado.getSelectedItem());
        return c;
    }

    private void registrarCliente() {
        Cliente c = construirClienteFormulario();
        c.setEstado("ACTIVO");

        List<String> errores = ValidadorCliente.validar(c);
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n", errores), "Errores de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (clienteDAO.existeCodigo(c.getCodigoCliente(), 0)) {
                JOptionPane.showMessageDialog(this, "El código de cliente ya existe en la BD.", "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (clienteDAO.existeDui(c.getDui(), 0)) {
                JOptionPane.showMessageDialog(this, "El DUI ya está registrado en la BD.", "DUI Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (clienteDAO.registrar(c)) {
                JOptionPane.showMessageDialog(this, "Cliente registrado exitosamente.");
                limpiarFormulario();
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo registrar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        try {
            List<Cliente> clientes = clienteDAO.listar();
            for (Cliente c : clientes) {
                modeloTabla.addRow(new Object[]{
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
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error al cargar clientes: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarCliente() {
        if (txtIdCliente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para modificar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente c = construirClienteFormulario();
        List<String> errores = ValidadorCliente.validar(c);
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(this, String.join("\n", errores), "Errores de Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (clienteDAO.existeCodigo(c.getCodigoCliente(), c.getIdCliente())) {
                JOptionPane.showMessageDialog(this, "El código pertenece a otro cliente.", "Código Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (clienteDAO.existeDui(c.getDui(), c.getIdCliente())) {
                JOptionPane.showMessageDialog(this, "El DUI pertenece a otro cliente.", "DUI Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (clienteDAO.modificar(c)) {
                JOptionPane.showMessageDialog(this, "Cliente modificado exitosamente.");
                limpiarFormulario();
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo modificar el cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cambiarEstadoCliente() {
        if (txtIdCliente.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la tabla para cambiar su estado.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idCliente = Integer.parseInt(txtIdCliente.getText().trim());
        String nuevoEstado = (String) cbxEstado.getSelectedItem();

        try {
            if (clienteDAO.cambiarEstado(idCliente, nuevoEstado)) {
                JOptionPane.showMessageDialog(this, "Estado actualizado a " + nuevoEstado + ".");
                limpiarFormulario();
                cargarDatosTabla();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo actualizar el estado.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error de base de datos: " + ex.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seleccionarFilaTabla() {
        int fila = tablaClientes.getSelectedRow();
        if (fila >= 0) {
            txtIdCliente.setText(modeloTabla.getValueAt(fila, 0).toString());
            txtCodigo.setText(modeloTabla.getValueAt(fila, 1).toString());
            txtDui.setText(modeloTabla.getValueAt(fila, 2).toString());
            txtNombre.setText(modeloTabla.getValueAt(fila, 3).toString());
            txtTelefono.setText(modeloTabla.getValueAt(fila, 4).toString());
            txtCorreo.setText(modeloTabla.getValueAt(fila, 5).toString());
            txtDireccion.setText(modeloTabla.getValueAt(fila, 6).toString());
            cbxEstado.setSelectedItem(modeloTabla.getValueAt(fila, 7).toString());
        }
    }

    private void limpiarFormulario() {
        txtIdCliente.setText("");
        txtCodigo.setText("");
        txtDui.setText("");
        txtNombre.setText("");
        txtTelefono.setText("");
        txtCorreo.setText("");
        txtDireccion.setText("");
        cbxEstado.setSelectedIndex(0);
        tablaClientes.clearSelection();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaClientes().setVisible(true);
        });
    }
}
