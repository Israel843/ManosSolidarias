package com.prdev.solidaryhands.network;

import com.prdev.solidaryhands.models.Donacion;
import com.prdev.solidaryhands.models.Fundacion;
import com.prdev.solidaryhands.models.Perfil;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface SupabaseDbService {

    // Consultar perfil de usuario por ID (eq.{user_id})
    @GET("rest/v1/profiles?select=*")
    Call<List<Perfil>> getProfile(@Query("id") String idFilter);

    // Crear o actualizar perfil en la tabla profiles
    @POST("rest/v1/profiles")
    Call<Void> upsertProfile(
            @Header("Prefer") String prefer,
            @Body Perfil perfil
    );

    // Obtener lista de fundaciones ordenadas por nombre
    @GET("rest/v1/foundations?select=*&order=nombre.asc")
    Call<List<Fundacion>> getFoundations();

    // Obtener historial de donaciones del usuario (user_id=eq.{user_id})
    @GET("rest/v1/donations?select=*&order=created_at.desc")
    Call<List<Donacion>> getDonations(@Query("user_id") String userIdFilter);

    // Insertar nueva donación
    @POST("rest/v1/donations")
    Call<Void> insertDonation(
            @Header("Prefer") String prefer,
            @Body Donacion donacion
    );
}
