package excepciones;

public class CuentaNoEncontradaException extends Exception {

    public CuentaNoEncontradaException(String mensaje) {
        super(mensaje);
    }

    public CuentaNoEncontradaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
