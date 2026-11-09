package vista;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipal extends JFrame {

    private JButton btnClientes;
    private JButton btnCuentas;
    private JButton btnSalir;

    private VentanaClientes ventanaClientes;
    private VentanaCuentas ventanaCuentas;

    public MenuPrincipal() {
        initComponents();
    }

    private void initComponents() {
        setTitle("Banco Académico UDB - Menú principal");
        setSize(420, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        JLabel lblTitulo = new JLabel("Sistema de Gestión Bancaria Académica", SwingConstants.CENTER);
        lblTitulo.setFont(lblTitulo.getFont().deriveFont(Font.BOLD, 16f));
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 10));

        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 10, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 60, 25, 60));

        btnClientes = new JButton("Administrar clientes");
        btnCuentas = new JButton("Registrar cuentas");
        btnSalir = new JButton("Salir");

        panelBotones.add(btnClientes);
        panelBotones.add(btnCuentas);
        panelBotones.add(btnSalir);

        add(lblTitulo, BorderLayout.NORTH);
        add(panelBotones, BorderLayout.CENTER);

        btnClientes.addActionListener(e -> abrirClientes());
        btnCuentas.addActionListener(e -> abrirCuentas());
        btnSalir.addActionListener(e -> System.exit(0));
    }

    private void abrirClientes() {
        if (ventanaClientes == null || !ventanaClientes.isDisplayable()) {
            ventanaClientes = new VentanaClientes();
        }
        ventanaClientes.setVisible(true);
        ventanaClientes.toFront();
    }

    private void abrirCuentas() {
        if (ventanaCuentas == null || !ventanaCuentas.isDisplayable()) {
            ventanaCuentas = new VentanaCuentas();
        }
        ventanaCuentas.setVisible(true);
        ventanaCuentas.toFront();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}
