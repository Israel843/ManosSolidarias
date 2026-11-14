package com.prdev.solidaryhands;

import com.prdev.solidaryhands.models.Donacion;
import java.util.ArrayList;
import java.util.List;

public class DonacionesRepositorio {

    private static final List<Donacion> donaciones = new ArrayList<>();

    public static void agregar(Donacion donacion) {
        donaciones.add(0, donacion);
    }

    public static List<Donacion> obtenerTodas() {
        return new ArrayList<>(donaciones);
    }

    public static int cantidadDonaciones() {
        return donaciones.size();
    }
}
