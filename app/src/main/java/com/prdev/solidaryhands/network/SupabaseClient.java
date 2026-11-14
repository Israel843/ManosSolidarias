package com.prdev.solidaryhands.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static SupabaseClient instance;
    private final SupabaseAuthService authService;
    private final SupabaseDbService dbService;

    private SupabaseClient(Context context) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor headerInterceptor = chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json");

            // Si el request ya incluye un Authorization explícito, lo conservamos.
            // De lo contrario, usamos el token de sesión o el anon key por defecto.
            if (original.header("Authorization") == null) {
                String userToken = SessionManager.getInstance(context).getAccessToken();
                if (userToken != null && !userToken.isEmpty()) {
                    requestBuilder.header("Authorization", "Bearer " + userToken);
                } else {
                    requestBuilder.header("Authorization", "Bearer " + SupabaseConfig.SUPABASE_ANON_KEY);
                }
            }

            return chain.proceed(requestBuilder.build());
        };

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.SUPABASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        authService = retrofit.create(SupabaseAuthService.class);
        dbService = retrofit.create(SupabaseDbService.class);
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
        return instance;
    }

    public SupabaseAuthService getAuthService() {
        return authService;
    }

    public SupabaseDbService getDbService() {
        return dbService;
    }
}
