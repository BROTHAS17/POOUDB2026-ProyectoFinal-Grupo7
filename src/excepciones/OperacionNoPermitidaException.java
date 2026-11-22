package excepciones;

/**
 * se lanza cuando una operación no cumple una regla del banco que no tiene
 * que ver con el monto, el saldo ni el estado de la cuenta,
 * por ejemplo transferir de una cuenta a la misma cuenta
 */
public class OperacionNoPermitidaException extends Exception {

    public OperacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }

    public OperacionNoPermitidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
