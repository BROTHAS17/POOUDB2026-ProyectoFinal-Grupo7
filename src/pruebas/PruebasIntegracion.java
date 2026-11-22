package pruebas;

import modelo.Cliente;
import validacion.ValidadorCliente;

import java.util.List;

public class PruebasIntegracion {

    public static void main(String[] args) {
        System.out.println("=== PRUEBAS DE VALIDACIÓN CON DATOS DE PERSONA 1 ===");

        Cliente c1 = new Cliente(1, "CLI001", "12345678-9", "Ana Lopez", "7000-0001", "ana.lopez@example.com", "San Salvador", "ACTIVO");
        List<String> errores1 = ValidadorCliente.validar(c1);
        System.out.println("Cliente 1 (Ana Lopez): Errores = " + errores1.size());

        Cliente c2 = new Cliente(0, "CLI003", "123456789", "Pedro Gomez", "7000-0003", "pedro@example.com", "Santa Tecla", "ACTIVO");
        List<String> errores2 = ValidadorCliente.validar(c2);
        System.out.println("Cliente 2 (DUI sin guion): Errores = " + errores2);

        Cliente c3 = new Cliente();
        List<String> errores3 = ValidadorCliente.validar(c3);
        System.out.println("Cliente 3 (Campos vacíos): Total errores = " + errores3.size());
    }
}
