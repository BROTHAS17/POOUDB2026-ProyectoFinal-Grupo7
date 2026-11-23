package vista;

import javax.swing.*;
import java.awt.*;
import java.util.function.Supplier;

public class MenuPrincipal extends JFrame {

    private JButton btnClientes;
    private JButton btnCuentas;
    private JButton btnDepositoRetiro;
    private JButton btnTransferencia;
    private JButton btnHistorial;
    private JButton btnConsultaMovimiento;
    private JButton btnReportes;
    private JButton btnSalir;

    // se guarda una referencia a cada ventana para no abrir dos copias de la misma
    private VentanaClientes ventanaClientes;
    private VentanaCuentas ventanaCuentas;
    private VentanaDepositoRetiro ventanaDepositoRetiro;
    private VentanaTransferencia ventanaTransferencia;
    private VentanaHistorial ventanaHistorial;
    private VentanaConsultaMovimiento ventanaConsultaMovimiento;
    private VentanaReportes ventanaReportes;

    public MenuPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Menú principal");
        setSize(460, 520);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Sistema de Gestión Bancaria Académica", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));

        btnClientes = new JButton("Administrar clientes");
        btnCuentas = new JButton("Registrar cuentas");
        btnDepositoRetiro = new JButton("Depósitos y retiros");
        btnTransferencia = new JButton("Transferencias");
        btnHistorial = new JButton("Historial de movimientos");
        btnConsultaMovimiento = new JButton("Consultar movimiento");
        btnReportes = new JButton("Reportes");
        btnSalir = new JButton("Salir");

        JPanel panelAdministracion = crearSeccion("Clientes y cuentas", btnClientes, btnCuentas);
        JPanel panelTransacciones = crearSeccion("Transacciones", btnDepositoRetiro, btnTransferencia);
        JPanel panelConsultas = crearSeccion("Consultas", btnHistorial, btnConsultaMovimiento, btnReportes);

        JPanel panelCentro = new JPanel();
        panelCentro.setLayout(new BoxLayout(panelCentro, BoxLayout.Y_AXIS));
        panelCentro.setBorder(BorderFactory.createEmptyBorder(5, 50, 0, 50));
        panelCentro.add(panelAdministracion);
        panelCentro.add(Box.createVerticalStrut(8));
        panelCentro.add(panelTransacciones);
        panelCentro.add(Box.createVerticalStrut(8));
        panelCentro.add(panelConsultas);

        JPanel panelSalir = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelSalir.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        panelSalir.add(btnSalir);

        add(lblTitulo, BorderLayout.NORTH);
        add(panelCentro, BorderLayout.CENTER);
        add(panelSalir, BorderLayout.SOUTH);

        btnClientes.addActionListener(e -> ventanaClientes = abrir(ventanaClientes, VentanaClientes::new));
        btnCuentas.addActionListener(e -> ventanaCuentas = abrir(ventanaCuentas, VentanaCuentas::new));
        btnDepositoRetiro.addActionListener(e ->
                ventanaDepositoRetiro = abrir(ventanaDepositoRetiro, VentanaDepositoRetiro::new));
        btnTransferencia.addActionListener(e ->
                ventanaTransferencia = abrir(ventanaTransferencia, VentanaTransferencia::new));
        btnHistorial.addActionListener(e -> ventanaHistorial = abrir(ventanaHistorial, VentanaHistorial::new));
        btnConsultaMovimiento.addActionListener(e ->
                ventanaConsultaMovimiento = abrir(ventanaConsultaMovimiento, VentanaConsultaMovimiento::new));
        btnReportes.addActionListener(e -> ventanaReportes = abrir(ventanaReportes, VentanaReportes::new));
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private JPanel crearSeccion(String titulo, JButton... botones) {
        JPanel panel = new JPanel(new GridLayout(botones.length, 1, 8, 8));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(titulo),
                BorderFactory.createEmptyBorder(4, 10, 8, 10)));
        for (JButton boton : botones) {
            panel.add(boton);
        }
        return panel;
    }

    /**
     * muestra VENTANA si ya está abierta, si no existe o ya se cerró crea una nueva con CREAR
     */
    private <T extends JFrame> T abrir(T ventana, Supplier<T> crear) {
        if (ventana == null || !ventana.isDisplayable()) {
            ventana = crear.get();
        }
        ventana.setVisible(true);
        ventana.toFront();
        return ventana;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
