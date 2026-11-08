package modelo;

/**
 * Clase de modelo que representa la entidad Cliente.
 */
public class Cliente {

    private int idCliente;
    private String codigoCliente;
    private String dui;
    private String nombreCompleto;
    private String telefono;
    private String correo;
    private String direccion;
    private String estado;

    public Cliente() {
    }

    public Cliente(int idCliente, String codigoCliente, String dui, String nombreCompleto,
                   String telefono, String correo, String direccion, String estado) {
        this.idCliente = idCliente;
        this.codigoCliente = codigoCliente;
        this.dui = dui;
        this.nombreCompleto = nombreCompleto;
        this.telefono = telefono;
        this.correo = correo;
        this.direccion = direccion;
        this.estado = estado;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getCodigoCliente() { return codigoCliente; }
    public void setCodigoCliente(String codigoCliente) { this.codigoCliente = codigoCliente; }

    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
