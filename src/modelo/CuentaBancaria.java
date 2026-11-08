package modelo;

import java.math.BigDecimal;
import java.sql.Date;

/**
 * Clase que representa una cuenta bancaria.
 */
public class CuentaBancaria {
    private int idCuenta;
    private String numeroCuenta;
    private String tipoCuenta;
    private Date fechaApertura;
    private BigDecimal saldo;
    private String estado;
    private int idCliente;

    private String nombreCliente;

    public CuentaBancaria() {
    }

    public CuentaBancaria(int idCuenta, String numeroCuenta, String tipoCuenta, Date fechaApertura,
                          BigDecimal saldo, String estado, int idCliente, String nombreCliente) {
        this.idCuenta = idCuenta;
        this.numeroCuenta = numeroCuenta;
        this.tipoCuenta = tipoCuenta;
        this.fechaApertura = fechaApertura;
        this.saldo = saldo;
        this.estado = estado;
        this.idCliente = idCliente;
        this.nombreCliente = nombreCliente;
    }

    public int getIdCuenta() { return idCuenta; }
    public void setIdCuenta(int idCuenta) { this.idCuenta = idCuenta; }

    public String getNumeroCuenta() { return numeroCuenta; }
    public void setNumeroCuenta(String numeroCuenta) { this.numeroCuenta = numeroCuenta; }

    public String getTipoCuenta() { return tipoCuenta; }
    public void setTipoCuenta(String tipoCuenta) { this.tipoCuenta = tipoCuenta; }

    public Date getFechaApertura() { return fechaApertura; }
    public void setFechaApertura(Date fechaApertura) { this.fechaApertura = fechaApertura; }

    public BigDecimal getSaldo() { return saldo; }
    public void setSaldo(BigDecimal saldo) { this.saldo = saldo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
}