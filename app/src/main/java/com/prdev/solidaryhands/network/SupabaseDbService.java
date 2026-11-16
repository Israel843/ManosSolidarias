package com.prdev.solidaryhands.network;

import com.prdev.solidaryhands.models.DonacionSupabase;
import com.prdev.solidaryhands.models.Fundacion;
import com.prdev.solidaryhands.models.Necesidad;
import com.prdev.solidaryhands.models.Perfil;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PATCH;
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

    @GET("rest/v1/fundaciones?select=*&order=nombre.asc")
    Call<List<Fundacion>> getFundaciones();

    @GET("rest/v1/fundaciones?select=*&limit=1")
    Call<List<Fundacion>> getFundacionPorUsuario(@Query("usuario_id") String usuarioIdFilter);

    @GET("rest/v1/necesidades")
    Call<List<Necesidad>> getNecesidadesActivas(
            @Query(value = "select", encoded = true) String select,
            @Query("estado") String estadoFilter
    );

    @GET("rest/v1/necesidades")
    Call<List<Necesidad>> getNecesidadesDeFundacion(
            @Query(value = "select", encoded = true) String select,
            @Query("fundacion_id") String fundacionIdFilter,
            @Query("order") String orden
    );

    @POST("rest/v1/necesidades")
    Call<List<Necesidad>> insertNecesidad(
            @Header("Prefer") String prefer,
            @Body Necesidad necesidad
    );

    @PATCH("rest/v1/necesidades")
    Call<Void> updateEstadoNecesidad(
            @Query("id") String idFilter,
            @Body Map<String, String> cambios
    );

    @GET("rest/v1/donaciones")
    Call<List<DonacionSupabase>> getDonacionesDonante(
            @Query(value = "select", encoded = true) String select,
            @Query("donante_id") String donanteIdFilter,
            @Query("order") String orden
    );

    @GET("rest/v1/donaciones")
    Call<List<DonacionSupabase>> getDonacionesFundacion(
            @Query(value = "select", encoded = true) String select,
            @Query("fundacion_id") String fundacionIdFilter,
            @Query("order") String orden
    );

    @POST("rest/v1/donaciones")
    Call<List<DonacionSupabase>> insertDonacion(
            @Header("Prefer") String prefer,
            @Body DonacionSupabase donacion
    );

    @PATCH("rest/v1/donaciones")
    Call<Void> updateEstadoDonacion(
            @Query("id") String idFilter,
            @Body Map<String, String> cambios
    );
}
