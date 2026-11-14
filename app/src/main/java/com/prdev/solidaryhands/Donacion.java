package com.prdev.solidaryhands;

public class Donacion extends com.prdev.solidaryhands.models.Donacion {

    public Donacion() {
        super();
    }

    public Donacion(String tipo, String categoria, String descripcion, String cantidad,
                    String unidad, String condicion, String entrega, String fundacion,
                    String fecha, String estado) {
        super(tipo, categoria, descripcion, cantidad, unidad, condicion, entrega, fundacion, fecha, estado);
    }

    public Donacion(String userId, String tipo, String categoria, String descripcion,
                    String cantidad, String unidad, String condicion, String entrega,
                    String fundacion, String fecha, String estado) {
        super(userId, tipo, categoria, descripcion, cantidad, unidad, condicion, entrega, fundacion, fecha, estado);
    }
}
