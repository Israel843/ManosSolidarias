package com.prdev.solidaryhands.models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.DecimalFormat;

public class Necesidad implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("fundacion_id")
    private Long fundacionId;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("cantidad")
    private Double cantidad;

    @SerializedName("unidad")
    private String unidad;

    @SerializedName("urgencia")
    private String urgencia;

    @SerializedName("estado")
    private String estado;

    @SerializedName("fecha_publicacion")
    private String fechaPublicacion;

    @SerializedName("fundacion")
    private FundacionResumen fundacion;

    public Necesidad() {}

    public Necesidad(Long fundacionId, String categoria, String descripcion, double cantidad,
                     String unidad, String urgencia) {
        this.fundacionId = fundacionId;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.urgencia = urgencia != null ? urgencia.toUpperCase() : "MEDIA";
        this.estado = "ACTIVA";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFundacionId() { return fundacionId; }
    public void setFundacionId(Long fundacionId) { this.fundacionId = fundacionId; }
    public String getCategoria() { return categoria != null ? categoria : ""; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescripcion() { return descripcion != null ? descripcion : ""; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Double getCantidadNumero() { return cantidad != null ? cantidad : 0d; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getUnidad() { return unidad != null ? unidad : "unidades"; }
    public void setUnidad(String unidad) { this.unidad = unidad; }
    public String getUrgencia() { return urgencia != null ? urgencia : "MEDIA"; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
    public String getEstado() { return estado != null ? estado : "ACTIVA"; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getFechaPublicacion() { return fechaPublicacion != null ? fechaPublicacion : ""; }
    public void setFechaPublicacion(String fechaPublicacion) { this.fechaPublicacion = fechaPublicacion; }
    public FundacionResumen getFundacionRelacion() { return fundacion; }
    public void setFundacionRelacion(FundacionResumen fundacion) { this.fundacion = fundacion; }

    public String getFundacion() {
        return fundacion != null && fundacion.nombre != null ? fundacion.nombre : "Fundación";
    }

    public String getCantidad() {
        double valor = getCantidadNumero();
        String numero = valor == Math.rint(valor)
                ? String.valueOf((long) valor)
                : new DecimalFormat("0.##").format(valor);
        return numero + " " + getUnidad();
    }

    public boolean isActiva() {
        return "ACTIVA".equalsIgnoreCase(getEstado());
    }

    public static class FundacionResumen implements Serializable {
        @SerializedName("nombre")
        private String nombre;

        public String getNombre() { return nombre != null ? nombre : ""; }
        public void setNombre(String nombre) { this.nombre = nombre; }
    }
}
