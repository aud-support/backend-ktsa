package com.ktsa.foosball.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @NotBlank(message = "Message is required")
    @Size(max = 2000, message = "Message must not exceed 2000 characters")
    private String message;

    /** Optional — which service the enquiry is about */
    private String subject;
}
