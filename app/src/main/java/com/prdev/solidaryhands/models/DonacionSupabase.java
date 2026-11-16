package com.prdev.solidaryhands.models;

import com.google.gson.annotations.SerializedName;

public class DonacionSupabase {
    @SerializedName("id")
    private Long id;
    @SerializedName("donante_id")
    private String donanteId;
    @SerializedName("fundacion_id")
    private Long fundacionId;
    @SerializedName("necesidad_id")
    private Long necesidadId;
    @SerializedName("tipo_objeto")
    private String tipoObjeto;
    @SerializedName("categoria")
    private String categoria;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("cantidad")
    private Double cantidad;
    @SerializedName("unidad")
    private String unidad;
    @SerializedName("condicion")
    private String condicion;
    @SerializedName("tipo_entrega")
    private String tipoEntrega;
    @SerializedName("puntaje_ia")
    private Double puntajeIa;
    @SerializedName("estado")
    private String estado;
    @SerializedName("fecha_registro")
    private String fechaRegistro;
    @SerializedName("fundacion")
    private FundacionResumen fundacion;

    public DonacionSupabase() {}

    public DonacionSupabase(String donanteId, Long fundacionId, Long necesidadId,
                            String tipoObjeto, String categoria, String descripcion,
                            double cantidad, String unidad, String condicion,
                            String tipoEntrega, Double puntajeIa, String estado) {
        this.donanteId = donanteId;
        this.fundacionId = fundacionId;
        this.necesidadId = necesidadId;
        this.tipoObjeto = tipoObjeto;
        this.categoria = categoria;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.condicion = condicion;
        this.tipoEntrega = tipoEntrega;
        this.puntajeIa = puntajeIa;
        this.estado = estado;
    }

    public Long getId() { return id; }
    public Long getFundacionId() { return fundacionId; }
    public String getEstado() { return estado != null ? estado : EstadoDonacion.ENVIADA; }

    public Donacion toDonacion() {
        String nombreFundacion = fundacion != null ? fundacion.nombre : "Fundación";
        String cantidadTexto = cantidad != null && cantidad == Math.rint(cantidad)
                ? String.valueOf(cantidad.longValue()) : String.valueOf(cantidad != null ? cantidad : 0);
        Donacion donacion = new Donacion(donanteId, tipoObjeto, categoria, descripcion,
                cantidadTexto, unidad, condicion, tipoEntrega, nombreFundacion,
                fechaRegistro != null ? fechaRegistro : "", getEstado());
        donacion.setId(id);
        return donacion;
    }

    public static class FundacionResumen {
        @SerializedName("nombre")
        private String nombre;
    }
}
