package com.university.takharrujy.presentation.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * FileResponse DTO
 * Response object containing project file metadata
 */
@Schema(description = "Project file metadata response")
public record FileResponse(
    
    @Schema(description = "File ID")
    Long id,
    
    @Schema(description = "Project ID this file belongs to")
    Long projectId,
    
    @Schema(description = "Stored filename")
    String filename,
    
    @Schema(description = "Original filename from upload")
    String originalFilename,
    
    @Schema(description = "File content type (MIME type)")
    String contentType,
    
    @Schema(description = "File size in bytes")
    Long fileSize,
    
    @Schema(description = "Storage path for file retrieval")
    String storagePath,
    
    @Schema(description = "User ID who uploaded the file")
    Long uploadedByUserId,
    
    @Schema(description = "Upload timestamp")
    Instant uploadedAt
) {
    
    /**
     * Get human-readable file size
     */
    public String getFormattedFileSize() {
        if (fileSize == null) return "0 B";
        
        long size = fileSize;
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return String.format("%.2f KB", size / 1024.0);
        if (size < 1024 * 1024 * 1024) return String.format("%.2f MB", size / (1024.0 * 1024));
        return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
    }
    
    /**
     * Check if file is an image
     */
    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * Check if file is a document
     */
    public boolean isDocument() {
        if (contentType == null) return false;
        return contentType.equals("application/pdf") ||
               contentType.contains("word") ||
               contentType.contains("document") ||
               contentType.contains("presentation") ||
               contentType.equals("text/plain");
    }
}

