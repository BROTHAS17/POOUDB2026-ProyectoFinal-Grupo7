package modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ReporteMovimiento {

    private int id;
    private String tipo;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private Integer cuentaOrigenId;
    private String numeroCuentaOrigen;
    private Integer cuentaDestinoId;
    private String numeroCuentaDestino;
    private BigDecimal saldoResultante;

    public ReporteMovimiento() {
    }

    public ReporteMovimiento(int id, String tipo, BigDecimal monto, LocalDateTime fecha,
                             Integer cuentaOrigenId, String numeroCuentaOrigen,
                             Integer cuentaDestinoId, String numeroCuentaDestino,
                             BigDecimal saldoResultante) {
        this.id = id;
        this.tipo = tipo;
        this.monto = monto;
        this.fecha = fecha;
        this.cuentaOrigenId = cuentaOrigenId;
        this.numeroCuentaOrigen = numeroCuentaOrigen;
        this.cuentaDestinoId = cuentaDestinoId;
        this.numeroCuentaDestino = numeroCuentaDestino;
        this.saldoResultante = saldoResultante;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public Integer getCuentaOrigenId() { return cuentaOrigenId; }
    public void setCuentaOrigenId(Integer cuentaOrigenId) { this.cuentaOrigenId = cuentaOrigenId; }

    public String getNumeroCuentaOrigen() { return numeroCuentaOrigen; }
    public void setNumeroCuentaOrigen(String numeroCuentaOrigen) { this.numeroCuentaOrigen = numeroCuentaOrigen; }

    public Integer getCuentaDestinoId() { return cuentaDestinoId; }
    public void setCuentaDestinoId(Integer cuentaDestinoId) { this.cuentaDestinoId = cuentaDestinoId; }

    public String getNumeroCuentaDestino() { return numeroCuentaDestino; }
    public void setNumeroCuentaDestino(String numeroCuentaDestino) { this.numeroCuentaDestino = numeroCuentaDestino; }

    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public void setSaldoResultante(BigDecimal saldoResultante) { this.saldoResultante = saldoResultante; }
}
