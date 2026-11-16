package com.prdev.solidaryhands.models;

public final class RolUsuario {
    public static final String DONANTE = "DONANTE";
    public static final String FUNDACION = "FUNDACION";

    private RolUsuario() {}

    public static String normalizar(String rol) {
        return FUNDACION.equalsIgnoreCase(rol) ? FUNDACION : DONANTE;
    }
}
