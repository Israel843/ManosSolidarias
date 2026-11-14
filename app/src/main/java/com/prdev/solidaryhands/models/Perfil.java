package com.prdev.solidaryhands.models;

import com.google.gson.annotations.SerializedName;

public class Perfil {
    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("apellido_paterno")
    private String apellidoPaterno;

    @SerializedName("apellido_materno")
    private String apellidoMaterno;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName("avatar_url")
    private String avatarUrl;

    public Perfil() {}

    public Perfil(String id, String email, String nombre, String apellidoPaterno, String apellidoMaterno, String telefono) {
        this.id = id;
        this.email = email;
        this.nombre = nombre;
        this.apellidoPaterno = apellidoPaterno;
        this.apellidoMaterno = apellidoMaterno;
        this.telefono = telefono;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidoPaterno() { return apellidoPaterno; }
    public void setApellidoPaterno(String apellidoPaterno) { this.apellidoPaterno = apellidoPaterno; }

    public String getApellidoMaterno() { return apellidoMaterno; }
    public void setApellidoMaterno(String apellidoMaterno) { this.apellidoMaterno = apellidoMaterno; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getNombreCompleto() {
        StringBuilder sb = new StringBuilder();
        if (nombre != null && !nombre.isEmpty()) sb.append(nombre);
        if (apellidoPaterno != null && !apellidoPaterno.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(apellidoPaterno);
        }
        if (apellidoMaterno != null && !apellidoMaterno.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(apellidoMaterno);
        }
        return sb.toString().trim();
    }
}
