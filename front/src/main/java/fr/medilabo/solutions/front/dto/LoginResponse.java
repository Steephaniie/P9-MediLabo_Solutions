package fr.medilabo.solutions.front.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String username;
    private String tokenType;
    private String error;

    public LoginResponse(String token, String username, String tokenType) {
        this.token = token;
        this.username = username;
        this.tokenType = tokenType;
        this.error = null;
    }
}