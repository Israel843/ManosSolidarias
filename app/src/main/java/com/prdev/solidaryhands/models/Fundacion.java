package com.prdev.solidaryhands.models;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Fundacion implements Serializable {
    @SerializedName("id")
    private Long id;

    @SerializedName("usuario_id")
    private String usuarioId;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("direccion")
    private String direccion;

    @SerializedName("ciudad")
    private String ciudad;

    @SerializedName("programas")
    private String programas;

    @SerializedName("imagen_url")
    private String imagenUrl;

    @SerializedName("telefono")
    private String telefono;

    @SerializedName(value = "correo", alternate = {"email"})
    private String email;

    public Fundacion() {}

    public Fundacion(String nombre, String descripcion, String direccion, String programas) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.programas = programas;
    }

    public Fundacion(Long id, String nombre, String descripcion, String direccion, String programas, String imagenUrl, String telefono, String email) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.direccion = direccion;
        this.programas = programas;
        this.imagenUrl = imagenUrl;
        this.telefono = telefono;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }

    public String getNombre() { return nombre != null ? nombre : ""; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion != null ? descripcion : ""; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDireccion() { return direccion != null ? direccion : ""; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad != null ? ciudad : ""; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public String getProgramas() { return programas != null ? programas : ""; }
    public void setProgramas(String programas) { this.programas = programas; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
