package com.hireme.authservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterDto {

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Mot de passe doit contenir au moins 8 caractères")
/*    @Pattern(regexp = ".*[A-Z].*", message = "Mot de passe doit contenir au moins une lettre majuscule")
    @Pattern(regexp = ".*[a-z].*", message = "Mot de passe doit contenir au moins une lettre minuscule")
    @Pattern(regexp = ".*\\d.*", message = "Mot de passe doit contenir au moins un chiffre")
    @Pattern(regexp = ".*[!@#$%^&*()].*", message = "Mot de passe doit contenir au moins un caractère spécial (!@#$%^&*())")*/
    private String password;


}
