package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.presentation.common.ApiResponse;
import com.university.takharrujy.presentation.dto.user.UniversityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public Controller
 * Handles public endpoints that don't require authentication
 */
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public", description = "Public endpoints accessible without authentication")
public class PublicController {

    private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

    private final UniversityRepository universityRepository;

    public PublicController(UniversityRepository universityRepository) {
        this.universityRepository = universityRepository;
    }

    @Operation(
        summary = "Get All Universities",
        description = "Retrieve list of all active universities for registration form dropdown"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Universities retrieved successfully",
            content = @Content(schema = @Schema(implementation = ApiResponse.class))
        )
    })
    @GetMapping("/universities")
    public ResponseEntity<ApiResponse<List<UniversityResponse>>> getUniversities() {
        
        logger.debug("Fetching all active universities for public access");
        
        List<UniversityResponse> universities = universityRepository.findByIsActiveTrue()
                .stream()
                .map(university -> new UniversityResponse(
                    university.getId(),
                    university.getName(),
                    university.getNameAr(),
                    university.getDomain(),
                    university.getContactEmail(),
                    university.getPhone(),
                    university.getAddress(),
                    university.getAddressAr(),
                    university.getIsActive()
                ))
                .sorted((u1, u2) -> u1.name().compareToIgnoreCase(u2.name())) // Sort by name
                .toList();
        
        logger.debug("Found {} active universities", universities.size());
        
        return ResponseEntity.ok(
            ApiResponse.success(universities, "Universities retrieved successfully")
        );
    }
}
