package validacion;

import modelo.Cliente;
import java.util.ArrayList;
import java.util.List;

public class ValidadorCliente {

    public static List<String> validar(Cliente c) {
        List<String> errores = new ArrayList<>();

        if (c == null) {
            errores.add("El objeto cliente no puede ser nulo.");
            return errores;
        }

        if (c.getCodigoCliente() == null || c.getCodigoCliente().trim().isEmpty()) {
            errores.add("El código de cliente es obligatorio.");
        } else if (c.getCodigoCliente().trim().length() > 15) {
            errores.add("El código de cliente no puede exceder los 15 caracteres.");
        }

        if (c.getDui() == null || c.getDui().trim().isEmpty()) {
            errores.add("El DUI es obligatorio.");
        } else if (!c.getDui().trim().matches("^\\d{8}-\\d$")) {
            errores.add("El DUI debe tener el formato 00000000-0 (8 dígitos, guion y 1 dígito).");
        }

        if (c.getNombreCompleto() == null || c.getNombreCompleto().trim().isEmpty()) {
            errores.add("El nombre completo es obligatorio.");
        } else if (c.getNombreCompleto().trim().length() > 120) {
            errores.add("El nombre completo no puede superar los 120 caracteres.");
        }

        if (c.getTelefono() == null || c.getTelefono().trim().isEmpty()) {
            errores.add("El teléfono es obligatorio.");
        } else if (c.getTelefono().trim().length() > 20) {
            errores.add("El teléfono no puede superar los 20 caracteres.");
        }

        if (c.getCorreo() == null || c.getCorreo().trim().isEmpty()) {
            errores.add("El correo electrónico es obligatorio.");
        } else if (c.getCorreo().trim().length() > 120) {
            errores.add("El correo electrónico no puede superar los 120 caracteres.");
        } else if (!c.getCorreo().trim().matches("^[\\w.+-]+@([\\w-]+\\.)+[A-Za-z]{2,}$")) {
            errores.add("El correo electrónico ingresado no tiene un formato válido.");
        }

        if (c.getDireccion() == null || c.getDireccion().trim().isEmpty()) {
            errores.add("La dirección es obligatoria.");
        } else if (c.getDireccion().trim().length() > 200) {
            errores.add("La dirección no puede superar los 200 caracteres.");
        }

        if (c.getEstado() == null || c.getEstado().trim().isEmpty()) {
            errores.add("El estado es obligatorio.");
        } else {
            String estadoUpper = c.getEstado().trim().toUpperCase();
            if (!estadoUpper.equals("ACTIVO") && !estadoUpper.equals("INACTIVO") && !estadoUpper.equals("BLOQUEADO")) {
                errores.add("El estado debe ser uno de los siguientes valores: ACTIVO, INACTIVO o BLOQUEADO.");
            }
        }

        return errores;
    }
}
