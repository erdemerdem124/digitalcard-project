// com.soliner.digitalcard.webApi.dto.auth.LoginResponse.java
package com.soliner.digitalcard.webApi.dto.auth;

import java.util.List;

public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long id; // <-- Bu satırı ekleyin
    private String username;
    private String email; // Eğer login yanıtında email de dönüyorsa
    private String firstName; // Eğer login yanıtında firstName de dönüyorsa
    private String lastName; // Eğer login yanıtında lastName de dönüyorsa
    private List<String> roles;

    // Constructor'ı güncelleyin
    public LoginResponse(String accessToken, String tokenType, Long id, String username, String email, String firstName, String lastName, List<String> roles) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.id = id; // <-- Constructor'a ekleyin
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    // Getter ve Setter'lar
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getId() { // <-- Getter ekleyin
        return id;
    }

    public void setId(Long id) { // <-- Setter ekleyin
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}