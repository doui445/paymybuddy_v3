package com.paymybuddy.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddConnectionDTO {

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;
}

