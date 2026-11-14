package com.prdev.solidaryhands.network;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import retrofit2.Response;

/**
 * Convierte la respuesta de error de Supabase en un mensaje claro para el usuario.
 * Antes la app siempre decía "credenciales incorrectas", aunque el problema fuera otro
 * (por ejemplo: correo sin confirmar).
 */
public final class SupabaseError {

    private SupabaseError() {}

    public static String mensajeLogin(Response<?> response) {
        String codigo = "";
        String msg = "";
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                codigo = texto(json, "error_code");
                if (codigo.isEmpty()) codigo = texto(json, "error");
                msg = texto(json, "msg");
                if (msg.isEmpty()) msg = texto(json, "error_description");
                if (msg.isEmpty()) msg = texto(json, "message");
            }
        } catch (Exception ignored) {
        }

        String lower = (codigo + " " + msg).toLowerCase();

        if (lower.contains("email_not_confirmed") || lower.contains("email not confirmed")) {
            return "Tu correo aún no está confirmado. Revisa tu bandeja de entrada (y spam) y abre el enlace de confirmación.";
        }
        if (lower.contains("invalid_credentials") || lower.contains("invalid login credentials")) {
            return "Correo o contraseña incorrectos.";
        }
        if (lower.contains("over_request_rate_limit") || response.code() == 429) {
            return "Demasiados intentos. Espera un momento e intenta de nuevo.";
        }
        if (response.code() == 401 || response.code() == 403) {
            return "No se pudo autenticar con el servidor (" + response.code()
                    + "). Revisa la clave pública de Supabase en SupabaseConfig.";
        }
        if (!msg.isEmpty()) {
            return msg + " (código " + response.code() + ")";
        }
        return "No se pudo iniciar sesión (código " + response.code() + ").";
    }

    public static String mensajeRegistro(Response<?> response) {
        String codigo = "";
        String msg = "";
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                codigo = texto(json, "error_code");
                msg = texto(json, "msg");
                if (msg.isEmpty()) msg = texto(json, "message");
            }
        } catch (Exception ignored) {
        }
        String lower = (codigo + " " + msg).toLowerCase();

        if (lower.contains("user_already_exists") || lower.contains("already registered")) {
            return "Ese correo ya está registrado. Intenta iniciar sesión.";
        }
        if (lower.contains("weak_password")) {
            return "La contraseña es muy débil. Usa al menos 6 caracteres.";
        }
        if (lower.contains("database error")) {
            return "Error en la base de datos al crear el perfil. Ejecuta el script SQL corregido en Supabase.";
        }
        if (lower.contains("over_email_send_rate_limit") || response.code() == 429) {
            return "Se enviaron demasiados correos. Espera unos minutos e intenta de nuevo.";
        }
        if (!msg.isEmpty()) {
            return msg + " (código " + response.code() + ")";
        }
        return "Error en el registro (código " + response.code() + ").";
    }

    private static String texto(JsonObject json, String clave) {
        JsonElement e = json.get(clave);
        return (e != null && !e.isJsonNull()) ? e.getAsString() : "";
    }
}
