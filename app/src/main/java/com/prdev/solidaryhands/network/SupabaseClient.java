package com.prdev.solidaryhands.network;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {

    private static SupabaseClient instance;
    private final SupabaseAuthService authService;
    private final SupabaseDbService dbService;

    private final Context appContext;
    // Cliente simple (sin interceptores) para renovar el token
    private final OkHttpClient refreshClient = new OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build();

    private SupabaseClient(Context context) {
        this.appContext = context;

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor headerInterceptor = chain -> {
            Request original = chain.request();
            SessionManager session = SessionManager.getInstance(appContext);

            // Las rutas /auth/v1/ (login, registro) SOLO llevan el apikey.
            // Antes se les enviaba también un Authorization con un token viejo o con la
            // clave pública, y eso podía hacer fallar el inicio de sesión.
            boolean esAuth = original.url().encodedPath().startsWith("/auth/v1/");
            boolean authExplicito = original.header("Authorization") != null;

            Request.Builder builder = original.newBuilder()
                    .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json");

            if (!esAuth && !authExplicito) {
                String token = session.getAccessToken();
                if (token != null && !token.isEmpty() && !"token_demo".equals(token)) {
                    builder.header("Authorization", "Bearer " + token);
                }
            }

            Response response = chain.proceed(builder.build());

            // Si el token venció (401), lo renovamos con el refresh token y reintentamos 1 vez
            if (response.code() == 401 && !esAuth && !authExplicito) {
                String refresh = session.getRefreshToken();
                if (refresh != null && !refresh.isEmpty()) {
                    String nuevoToken = renovarToken(refresh);
                    if (nuevoToken != null) {
                        response.close();
                        Request reintento = original.newBuilder()
                                .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                                .header("Content-Type", "application/json")
                                .header("Authorization", "Bearer " + nuevoToken)
                                .build();
                        return chain.proceed(reintento);
                    }
                }
            }
            return response;
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

    /** Renueva el access token. Devuelve el token nuevo o null si no se pudo. */
    private synchronized String renovarToken(String refreshToken) {
        try {
            JSONObject cuerpo = new JSONObject();
            cuerpo.put("refresh_token", refreshToken);

            Request request = new Request.Builder()
                    .url(SupabaseConfig.SUPABASE_URL + "auth/v1/token?grant_type=refresh_token")
                    .header("apikey", SupabaseConfig.SUPABASE_ANON_KEY)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(cuerpo.toString(), MediaType.parse("application/json")))
                    .build();

            try (Response r = refreshClient.newCall(request).execute()) {
                if (!r.isSuccessful() || r.body() == null) {
                    return null;
                }
                JSONObject json = new JSONObject(r.body().string());
                String nuevoAcceso = json.optString("access_token", "");
                String nuevoRefresh = json.optString("refresh_token", "");
                if (nuevoAcceso.isEmpty()) {
                    return null;
                }
                SessionManager.getInstance(appContext).updateTokens(
                        nuevoAcceso, nuevoRefresh.isEmpty() ? null : nuevoRefresh);
                return nuevoAcceso;
            }
        } catch (IOException | JSONException e) {
            return null;
        }
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
