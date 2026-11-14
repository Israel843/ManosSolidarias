package com.prdev.solidaryhands.network;

import com.prdev.solidaryhands.models.AuthRequest;
import com.prdev.solidaryhands.models.AuthResponse;
import com.prdev.solidaryhands.models.SignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface SupabaseAuthService {

    // Registro en Supabase Auth (GoTrue)
    @POST("auth/v1/signup")
    Call<AuthResponse> signUp(@Body SignUpRequest request);

    // Inicio de sesión con correo y contraseña
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> signIn(@Body AuthRequest request);

    // Cierre de sesión
    @POST("auth/v1/logout")
    Call<Void> signOut(@Header("Authorization") String bearerToken);
}
