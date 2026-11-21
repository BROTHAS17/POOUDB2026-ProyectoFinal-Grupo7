package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Movimiento {

    private int id;
    private String tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private Integer cuentaOrigenId;
    private Integer cuentaDestinoId;
    private BigDecimal saldoResultante;

    public Movimiento() {
    }

    public Movimiento(String tipo,
                      BigDecimal monto,
                      Integer cuentaOrigenId,
                      Integer cuentaDestinoId,
                      BigDecimal saldoResultante) {
        this.tipo = tipo;
        this.monto = monto;
        this.cuentaOrigenId = cuentaOrigenId;
        this.cuentaDestinoId = cuentaDestinoId;
        this.saldoResultante = saldoResultante;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Integer getCuentaOrigenId() {
        return cuentaOrigenId;
    }

    public void setCuentaOrigenId(Integer cuentaOrigenId) {
        this.cuentaOrigenId = cuentaOrigenId;
    }

    public Integer getCuentaDestinoId() {
        return cuentaDestinoId;
    }

    public void setCuentaDestinoId(Integer cuentaDestinoId) {
        this.cuentaDestinoId = cuentaDestinoId;
    }

    public BigDecimal getSaldoResultante() {
        return saldoResultante;
    }

    public void setSaldoResultante(BigDecimal saldoResultante) {
        this.saldoResultante = saldoResultante;
    }
}
