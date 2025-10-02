package com.university.takharrujy.presentation.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload to create a new university")
public record UniversityCreateRequest(

        @Schema(description = "University name in English", example = "Cairo University")
        @NotBlank
        String name,

        @Schema(description = "University name in Arabic", example = "جامعة القاهرة")
        @NotBlank
        String nameAr,

        @Schema(description = "University domain", example = "cu.edu.eg")
        @NotBlank
        String domain,

        @Schema(description = "Contact email", example = "info@cu.edu.eg")
        String contactEmail,

        @Schema(description = "Phone number", example = "+20 2 12345678")
        String phone,

        @Schema(description = "Address in English", example = "Giza, Egypt")
        String address,

        @Schema(description = "Address in Arabic", example = "الجيزة، مصر")
        String addressAr
) {}
