package com.prdev.solidaryhands;

public class Donacion {

    private String tipo;
    private String categoria;
    private String descripcion;
    private String cantidad;
    private String unidad;
    private String condicion;
    private String entrega;
    private String fundacion;
    private String fecha;
    private String estado;

    public Donacion(
            String tipo,
            String categoria,
            String descripcion,
            String cantidad,
            String unidad,
            String condicion,
            String entrega,
            String fundacion,
            String fecha,
            String estado) {

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

    public String getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCantidad() {
        return cantidad;
    }

    public String getUnidad() {
        return unidad;
    }

    public String getCondicion() {
        return condicion;
    }

    public String getEntrega() {
        return entrega;
    }

    public String getFundacion() {
        return fundacion;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }
}
