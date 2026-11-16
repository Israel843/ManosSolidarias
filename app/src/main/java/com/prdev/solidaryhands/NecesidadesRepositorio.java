package com.prdev.solidaryhands;

import com.prdev.solidaryhands.models.Necesidad;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Funciones de ordenamiento para las necesidades obtenidas desde Supabase. */
public final class NecesidadesRepositorio {
    private NecesidadesRepositorio() {}

    public static List<Necesidad> ordenar(List<Necesidad> origen, String categoria) {
        List<Necesidad> resultado = origen != null
                ? new ArrayList<>(origen) : new ArrayList<>();
        resultado.sort(Comparator
                .comparingInt((Necesidad n) -> coincide(n, categoria) ? 0 : 1)
                .thenComparingInt(NecesidadesRepositorio::prioridadUrgencia)
                .thenComparing(Necesidad::getFechaPublicacion, Comparator.reverseOrder()));
        return resultado;
    }

    public static boolean coincide(Necesidad necesidad, String categoria) {
        return !normalizar(categoria).isEmpty()
                && normalizar(necesidad.getCategoria()).equals(normalizar(categoria));
    }

    private static String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase(Locale.ROOT);
    }

    private static int prioridadUrgencia(Necesidad necesidad) {
        if ("ALTA".equalsIgnoreCase(necesidad.getUrgencia())) return 0;
        if ("MEDIA".equalsIgnoreCase(necesidad.getUrgencia())) return 1;
        return 2;
    }
}
