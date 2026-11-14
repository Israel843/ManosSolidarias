package com.prdev.solidaryhands;

public class Fundacion extends com.prdev.solidaryhands.models.Fundacion {

    public Fundacion() {
        super();
    }

    public Fundacion(String nombre, String descripcion, String direccion, String programas) {
        super(nombre, descripcion, direccion, programas);
    }

    public Fundacion(Long id, String nombre, String descripcion, String direccion, String programas,
                     String imagenUrl, String telefono, String email) {
        super(id, nombre, descripcion, direccion, programas, imagenUrl, telefono, email);
    }
}
