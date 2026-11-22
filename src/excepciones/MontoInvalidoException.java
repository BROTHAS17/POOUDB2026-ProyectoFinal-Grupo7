package excepciones;

public class MontoInvalidoException extends Exception {

    public MontoInvalidoException(String mensaje) {
        super(mensaje);
    }

    public MontoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
