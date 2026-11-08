package validacion;

import modelo.CuentaBancaria;

import java.util.ArrayList;
import java.util.List;

public class ValidadorCuenta {
    public static List<String> validar(CuentaBancaria c) {
        List<String> errores = new ArrayList<>();

        if (c == null) {
            errores.add("El objeto cuenta no puede ser nulo.");
            return errores;
        }

        if (c.getNumeroCuenta() == null || c.getNumeroCuenta().trim().isEmpty()) {
            errores.add("El número de cuenta es obligatorio.");
        } else if (c.getNumeroCuenta().trim().length() > 20) {
            errores.add("El número de cuenta no puede exceder los 20 caracteres.");
        }

        if (c.getIdCliente() <= 0) {
            errores.add("Debe seleccionar un cliente válido.");
        }

        return errores;
    }
}