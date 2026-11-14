package com.prdev.solidaryhands.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Donacion implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("categoria")
    private String categoria;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("cantidad")
    private String cantidad;

    @SerializedName("unidad")
    private String unidad;

    @SerializedName("condicion")
    private String condicion;

    @SerializedName("entrega")
    private String entrega;

    @SerializedName("fundacion")
    private String fundacion;

    @SerializedName("fecha")
    private String fecha;

    @SerializedName("estado")
    private String estado;

    public Donacion() {}

    public Donacion(String tipo, String categoria, String descripcion, String cantidad,
                    String unidad, String condicion, String entrega, String fundacion,
                    String fecha, String estado) {
        this.tipo = tipo;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.condicion = condicion;
        this.entrega = entrega;
        this.fundacion = fundacion;
        this.fecha = fecha;
        this.estado = estado;
    }

    public Donacion(String userId, String tipo, String categoria, String descripcion,
                    String cantidad, String unidad, String condicion, String entrega,
                    String fundacion, String fecha, String estado) {
        this(tipo, categoria, descripcion, cantidad, unidad, condicion, entrega, fundacion, fecha, estado);
        this.userId = userId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTipo() { return tipo != null ? tipo : ""; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getCategoria() { return categoria != null ? categoria : ""; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getDescripcion() { return descripcion != null ? descripcion : ""; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getCantidad() { return cantidad != null ? cantidad : ""; }
    public void setCantidad(String cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad != null ? unidad : ""; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getCondicion() { return condicion != null ? condicion : ""; }
    public void setCondicion(String condicion) { this.condicion = condicion; }

    public String getEntrega() { return entrega != null ? entrega : ""; }
    public void setEntrega(String entrega) { this.entrega = entrega; }

    public String getFundacion() { return fundacion != null ? fundacion : ""; }
    public void setFundacion(String fundacion) { this.fundacion = fundacion; }

    public String getFecha() { return fecha != null ? fecha : ""; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getEstado() { return estado != null ? estado : "Pendiente de revisión"; }
    public void setEstado(String estado) { this.estado = estado; }
}
