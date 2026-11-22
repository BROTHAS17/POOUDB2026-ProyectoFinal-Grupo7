package pruebas;

import dao.ReporteDAO;
import modelo.Cliente;
import modelo.CuentaBancaria;
import modelo.ReporteMovimiento;
import modelo.ResumenTotales;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vista.VentanaReportes;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class PruebasReportes {

    private static final Logger logger = LogManager.getLogger(PruebasReportes.class);

    public static void main(String[] args) {
        boolean modoOffline = args.length > 0 && "--offline".equalsIgnoreCase(args[0]);

        ejecutarPruebasOffline();

        if (!modoOffline) {
            ejecutarPruebasBaseDatos();
        }
    }

    private static void ejecutarPruebasOffline() {
        System.out.println("=== PRUEBAS OFFLINE DEL MÓDULO DE REPORTES ===");
        logger.info("Iniciando pruebas offline del módulo de reportes");

        ResumenTotales resumen = new ResumenTotales();
        resumen.setTotalDepositado(new BigDecimal("2300.00"));
        resumen.setCantidadDepositos(2);
        resumen.setTotalRetirado(BigDecimal.ZERO);
        resumen.setCantidadRetiros(0);
        resumen.setTotalTransferido(new BigDecimal("200.00"));
        resumen.setCantidadTransferencias(1);

        if (resumen.getTotalMovimientos() != 3) {
            throw new AssertionError("Se esperaban 3 movimientos, obtenido: " + resumen.getTotalMovimientos());
        }
        if (resumen.getMontoTotalOperado().compareTo(new BigDecimal("2500.00")) != 0) {
            throw new AssertionError("Monto total operado inesperado: " + resumen.getMontoTotalOperado());
        }

        ReporteMovimiento rm = new ReporteMovimiento(
                1, "TRANSFERENCIA", new BigDecimal("200.00"), LocalDateTime.now(),
                2, "CTA-PRB-01", 3, "CTA-PRB-02", new BigDecimal("1300.00")
        );
        if (!"CTA-PRB-01".equals(rm.getNumeroCuentaOrigen()) || !"CTA-PRB-02".equals(rm.getNumeroCuentaDestino())) {
            throw new AssertionError("Números de cuenta no coinciden en ReporteMovimiento");
        }

        LocalDate fechaValida = VentanaReportes.parsearFechaOpcional("2026-11-21", "Desde");
        if (!LocalDate.of(2026, 11, 21).equals(fechaValida)) {
            throw new AssertionError("Error al parsear fecha válida");
        }
        if (VentanaReportes.parsearFechaOpcional("   ", "Desde") != null) {
            throw new AssertionError("Fecha vacía debería retornar null");
        }

        boolean rechazoFechaInvalida = false;
        try {
            VentanaReportes.parsearFechaOpcional("21/11/2026", "Desde");
        } catch (IllegalArgumentException ex) {
            rechazoFechaInvalida = true;
        }
        if (!rechazoFechaInvalida) {
            throw new AssertionError("Fecha con formato inválido debió lanzar IllegalArgumentException");
        }

        System.out.println("ResumenTotales OK: movimientos=" + resumen.getTotalMovimientos()
                + ", operado=$" + resumen.getMontoTotalOperado());
        System.out.println("ReporteMovimiento OK: " + rm.getNumeroCuentaOrigen() + " -> " + rm.getNumeroCuentaDestino());
        System.out.println("Validación de filtros de fecha OK");
        logger.info("Pruebas offline del módulo de reportes finalizadas con éxito");
    }

    private static void ejecutarPruebasBaseDatos() {
        System.out.println("\n=== PRUEBAS CON MYSQL DEL MÓDULO DE REPORTES ===");
        ReporteDAO dao = new ReporteDAO();
        try {
            List<CuentaBancaria> activas = dao.listarCuentasPorEstado("ACTIVA", "TODOS");
            List<CuentaBancaria> bloqueadas = dao.listarCuentasPorEstado("BLOQUEADA", "TODOS");
            List<Cliente> clientes = dao.listarClientesRegistrados("TODOS", "");
            List<ReporteMovimiento> historial = dao.listarHistorialTransacciones("TODOS", null, null);
            ResumenTotales totales = dao.obtenerResumenTotales();

            System.out.println("1. Cuentas activas: " + activas.size());
            System.out.println("2. Cuentas bloqueadas: " + bloqueadas.size());
            System.out.println("3. Clientes registrados: " + clientes.size());
            System.out.println("4. Historial de transacciones: " + historial.size());
            if (!activas.isEmpty()) {
                CuentaBancaria primera = activas.get(0);
                List<ReporteMovimiento> porCuenta = dao.listarTransaccionesPorCuenta(primera.getIdCuenta(), "TODOS");
                System.out.println("5. Transacciones de " + primera.getNumeroCuenta() + ": " + porCuenta.size());
            }
            System.out.println("6. Total depositado: $" + totales.getTotalDepositado());
            System.out.println("7. Total retirado: $" + totales.getTotalRetirado());
            System.out.println("8. Total transferido: $" + totales.getTotalTransferido());
        } catch (SQLException ex) {
            logger.error("Error durante las pruebas con MySQL: {}", ex.getMessage(), ex);
            System.err.println("Error SQL: " + ex.getMessage());
        }
    }
}
