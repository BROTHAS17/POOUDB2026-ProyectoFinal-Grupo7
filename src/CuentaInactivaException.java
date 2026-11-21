package excepciones;

public class CuentaInactivaException extends Exception {

    public CuentaInactivaException(String mensaje) {
        super(mensaje);
    }

    public CuentaInactivaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
