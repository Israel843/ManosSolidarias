package com.prdev.solidaryhands;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.prdev.solidaryhands.models.Donacion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Persistencia local temporal hasta conectar el nuevo esquema de Supabase. */
public final class DonacionesRepositorio {
    private static final String PREF = "donaciones_locales";
    private static final String KEY = "lista";
    private static final Gson GSON = new Gson();

    private DonacionesRepositorio() {}

    public static synchronized void agregar(Context context, Donacion donacion) {
        List<Donacion> donaciones = obtenerTodas(context);
        if (donacion.getId() == null) donacion.setId(System.currentTimeMillis());
        donaciones.add(0, donacion);
        guardar(context, donaciones);
    }

    public static synchronized List<Donacion> obtenerTodas(Context context) {
        SharedPreferences pref = context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String json = pref.getString(KEY, "[]");
        Type tipo = new TypeToken<List<Donacion>>() {}.getType();
        List<Donacion> lista = GSON.fromJson(json, tipo);
        return lista != null ? new ArrayList<>(lista) : new ArrayList<>();
    }

    public static synchronized boolean actualizarEstado(Context context, Long id, String estado) {
        if (id == null) return false;
        List<Donacion> lista = obtenerTodas(context);
        for (Donacion donacion : lista) {
            if (id.equals(donacion.getId())) {
                donacion.setEstado(estado);
                guardar(context, lista);
                return true;
            }
        }
        return false;
    }

    public static synchronized int cantidadDonaciones(Context context) {
        return obtenerTodas(context).size();
    }

    private static void guardar(Context context, List<Donacion> donaciones) {
        context.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .edit().putString(KEY, GSON.toJson(donaciones)).apply();
    }
}
