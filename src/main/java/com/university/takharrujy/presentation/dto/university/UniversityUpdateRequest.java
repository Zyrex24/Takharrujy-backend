package com.university.takharrujy.presentation.dto.university;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

@Schema(name = "UniversityUpdateRequest", description = "Request payload for updating a university")
public record UniversityUpdateRequest(

        @Schema(description = "University name in English", example = "Cairo University")
        String name,

        @Schema(description = "University name in Arabic", example = "جامعة القاهرة")
        String nameAr,

        @Schema(description = "University email domain", example = "cu.edu.eg")
        String domain,

        @Schema(description = "Contact email of the university", example = "info@cu.edu.eg")
        String contactEmail,

        @Schema(description = "University phone number", example = "+20 2 3567 1234")
        String phone,

        @Schema(description = "University address in English", example = "Giza, Cairo, Egypt")
        String address,

        @Schema(description = "University address in Arabic", example = "الجيزة، القاهرة، مصر")
        String addressAr,

        @Schema(description = "Whether the university is active", example = "true")
        Boolean isActive
) {}
