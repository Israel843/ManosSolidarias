package com.prdev.solidaryhands.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class SignUpRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("data")
    private Map<String, Object> data;

    public SignUpRequest(String email, String password, Map<String, Object> data) {
        this.email = email;
        this.password = password;
        this.data = data;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Map<String, Object> getData() { return data; }
    public void setData(Map<String, Object> data) { this.data = data; }
}
