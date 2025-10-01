package com.university.takharrujy.presentation.controller;

import com.university.takharrujy.application.service.ProjectFileService;
import com.university.takharrujy.domain.entity.ProjectFile;
import com.university.takharrujy.presentation.dto.common.ApiResponse;
import com.university.takharrujy.presentation.dto.file.FileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * FileController
 * REST API endpoints for project file management
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "File Management", description = "Project file upload, listing, download, and deletion with virus scanning")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173", "https://takharrujy.com"})
public class FileController {

    private final ProjectFileService fileService;

    public FileController(ProjectFileService fileService) {
        this.fileService = fileService;
    }

    /**
     * Upload file to project
     */
    @PostMapping(value = "/projects/{projectId}/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR','ADMIN')")
    @Operation(summary = "Upload file to project", 
              description = "Upload a file to a specific project with virus scanning and validation. Max size: 100MB")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "File uploaded successfully",
            content = @Content(schema = @Schema(implementation = FileResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid file or validation error"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "413", 
            description = "File too large - max 100MB"
        )
    })
    public ResponseEntity<ApiResponse<FileResponse>> upload(
            @Parameter(description = "Project ID to upload file to")
            @PathVariable Long projectId,
            @Parameter(description = "File to upload (PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, ZIP, TXT, images)")
            @RequestParam("file") MultipartFile file
    ) {
        Long userId = getCurrentUserId();
        FileResponse response = fileService.upload(projectId, userId, file);
        
        return ResponseEntity.ok(
            ApiResponse.success(response, "File uploaded successfully", "تم رفع الملف بنجاح")
        );
    }

    /**
     * List project files
     */
    @GetMapping("/projects/{projectId}/files")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR','ADMIN')")
    @Operation(summary = "List project files", 
              description = "Get list of all files uploaded to a specific project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Files listed successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    public ResponseEntity<ApiResponse<List<FileResponse>>> listFiles(
            @Parameter(description = "Project ID to list files from")
            @PathVariable Long projectId
    ) {
        List<FileResponse> files = fileService.listByProject(projectId);
        
        return ResponseEntity.ok(
            ApiResponse.success(files, "Files listed successfully", "تم سرد الملفات بنجاح")
        );
    }

    /**
     * Get file metadata
     */
    @GetMapping("/files/{fileId}/info")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR','ADMIN')")
    @Operation(summary = "Get file metadata", 
              description = "Get detailed metadata for a specific file")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "File metadata retrieved successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "File not found"
        )
    })
    public ResponseEntity<ApiResponse<FileResponse>> getFileInfo(
            @Parameter(description = "File ID")
            @PathVariable Long fileId
    ) {
        FileResponse file = fileService.getFileMetadata(fileId);
        
        return ResponseEntity.ok(
            ApiResponse.success(file, "File metadata retrieved", "تم استرداد بيانات الملف")
        );
    }

    /**
     * Download file
     */
    @GetMapping("/files/{fileId}")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR','ADMIN')")
    @Operation(summary = "Download file", 
              description = "Download a file by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "File downloaded successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "File not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    public ResponseEntity<Resource> download(
            @Parameter(description = "File ID to download")
            @PathVariable Long fileId
    ) {
        Resource resource = fileService.download(fileId);
        ProjectFile file = fileService.getFileForDownload(fileId);
        
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(file.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename=\"" + file.getOriginalFilename() + "\"")
            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(file.getFileSize()))
            .body(resource);
    }

    /**
     * Delete file
     */
    @DeleteMapping("/files/{fileId}")
    @PreAuthorize("hasAnyRole('STUDENT','SUPERVISOR','ADMIN')")
    @Operation(summary = "Delete file", 
              description = "Delete a file by its ID (removes from storage and database)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "File deleted successfully"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "File not found"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Unauthorized - Invalid or expired token"
        )
    })
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "File ID to delete")
            @PathVariable Long fileId
    ) {
        fileService.delete(fileId);
        
        return ResponseEntity.ok(
            ApiResponse.success(null, "File deleted successfully", "تم حذف الملف بنجاح")
        );
    }

    /**
     * Get current user ID from security context
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            } else if (principal instanceof String) {
                try {
                    return Long.parseLong((String) principal);
                } catch (NumberFormatException e) {
                    throw new com.university.takharrujy.infrastructure.exception.SecurityException(
                        "Invalid user ID in token");
                }
            }
        }
        throw new com.university.takharrujy.infrastructure.exception.SecurityException(
            "No authenticated user found");
    }
}

