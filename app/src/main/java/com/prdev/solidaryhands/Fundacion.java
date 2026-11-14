package com.prdev.solidaryhands;

public class Fundacion {

    private String nombre;
    private String descripcion;
    private String direccion;
    private String programas;

    public Fundacion(
            String nombre,
            String descripcion,
            String direccion,
            String programas) {

        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.programas = programas;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getProgramas() {
        return programas;
    }
}
