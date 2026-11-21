package modelo;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ResumenTotales {

    private BigDecimal totalDepositado;
    private BigDecimal totalRetirado;
    private BigDecimal totalTransferido;
    private int cantidadDepositos;
    private int cantidadRetiros;
    private int cantidadTransferencias;

    public ResumenTotales() {
        BigDecimal cero = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.totalDepositado = cero;
        this.totalRetirado = cero;
        this.totalTransferido = cero;
    }

    public BigDecimal getTotalDepositado() {
        return totalDepositado;
    }

    public void setTotalDepositado(BigDecimal totalDepositado) {
        this.totalDepositado = normalizar(totalDepositado);
    }

    public BigDecimal getTotalRetirado() {
        return totalRetirado;
    }

    public void setTotalRetirado(BigDecimal totalRetirado) {
        this.totalRetirado = normalizar(totalRetirado);
    }

    public BigDecimal getTotalTransferido() {
        return totalTransferido;
    }

    public void setTotalTransferido(BigDecimal totalTransferido) {
        this.totalTransferido = normalizar(totalTransferido);
    }

    public int getCantidadDepositos() {
        return cantidadDepositos;
    }

    public void setCantidadDepositos(int cantidadDepositos) {
        this.cantidadDepositos = cantidadDepositos;
    }

    public int getCantidadRetiros() {
        return cantidadRetiros;
    }

    public void setCantidadRetiros(int cantidadRetiros) {
        this.cantidadRetiros = cantidadRetiros;
    }

    public int getCantidadTransferencias() {
        return cantidadTransferencias;
    }

    public void setCantidadTransferencias(int cantidadTransferencias) {
        this.cantidadTransferencias = cantidadTransferencias;
    }

    public int getTotalMovimientos() {
        return cantidadDepositos + cantidadRetiros + cantidadTransferencias;
    }

    public BigDecimal getMontoTotalOperado() {
        return totalDepositado.add(totalRetirado).add(totalTransferido);
    }

    private BigDecimal normalizar(BigDecimal valor) {
        return (valor != null ? valor : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
