package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.ProjectFile;
import com.university.takharrujy.domain.repository.ProjectFileRepository;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.infrastructure.exception.ValidationException;
import com.university.takharrujy.presentation.dto.file.FileResponse;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ProjectFileService
 * Business logic for project file management including upload, download, listing, and deletion
 */
@Service
@Transactional
public class ProjectFileService {

    private static final long MAX_FILE_SIZE = 100L * 1024 * 1024; // 100MB
    
    private static final String[] ALLOWED_CONTENT_TYPES = new String[] {
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "application/zip",
        "application/x-zip-compressed",
        "text/plain",
        "image/jpeg",
        "image/png",
        "image/webp",
        "image/gif"
    };

    private final ProjectFileRepository repository;
    private final FileStorageService storage;
    private final VirusScanService virusScanService;

    public ProjectFileService(ProjectFileRepository repository,
                              FileStorageService storage,
                              VirusScanService virusScanService) {
        this.repository = repository;
        this.storage = storage;
        this.virusScanService = virusScanService;
    }

    /**
     * Upload a file to a project
     */
    public FileResponse upload(Long projectId, Long userId, MultipartFile file) {
        // Validate file
        validateFile(file);

        // Perform virus scan
        if (!virusScanService.scanFile(file)) {
            throw new ValidationException("File failed security scan - potential malware detected");
        }

        // Store file
        String storagePath = storage.storeProjectFile(file, projectId);

        // Create entity
        ProjectFile entity = new ProjectFile();
        entity.setProjectId(projectId);
        entity.setUploadedByUserId(userId);
        entity.setOriginalFilename(file.getOriginalFilename());
        entity.setContentType(safeContentType(file.getContentType()));
        entity.setFileSize(file.getSize());
        entity.setStoragePath(storagePath);
        entity.setFilename(extractFilename(storagePath));

        // Save to database
        ProjectFile saved = repository.save(entity);
        
        return toResponse(saved);
    }

    /**
     * List all files for a project
     */
    @Transactional(readOnly = true)
    public List<FileResponse> listByProject(Long projectId) {
        return repository.findByProjectIdOrderByUploadedAtDesc(projectId)
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get file metadata by ID
     */
    @Transactional(readOnly = true)
    public FileResponse getFileMetadata(Long fileId) {
        ProjectFile file = getFileById(fileId);
        return toResponse(file);
    }

    /**
     * Download a file
     */
    @Transactional(readOnly = true)
    public Resource download(Long fileId) {
        ProjectFile file = getFileById(fileId);
        return storage.loadAsResource(file.getStoragePath());
    }

    /**
     * Get file entity for download metadata
     */
    @Transactional(readOnly = true)
    public ProjectFile getFileForDownload(Long fileId) {
        return getFileById(fileId);
    }

    /**
     * Delete a file
     */
    public void delete(Long fileId) {
        ProjectFile file = getFileById(fileId);
        
        // Delete from storage
        storage.deleteFile(file.getStoragePath());
        
        // Delete from database
        repository.deleteById(fileId);
    }

    /**
     * Get file statistics for a project
     */
    @Transactional(readOnly = true)
    public long getProjectFileCount(Long projectId) {
        return repository.countByProjectId(projectId);
    }

    /**
     * Helper: Get file by ID or throw exception
     */
    private ProjectFile getFileById(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));
    }

    /**
     * Helper: Validate file upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("File is required");
        }
        
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("File size cannot exceed 100MB");
        }
        
        String contentType = safeContentType(file.getContentType());
        boolean isAllowed = false;
        for (String allowedType : ALLOWED_CONTENT_TYPES) {
            if (allowedType.equalsIgnoreCase(contentType)) {
                isAllowed = true;
                break;
            }
        }
        
        if (!isAllowed) {
            throw new ValidationException(
                "Unsupported file type: " + contentType + 
                ". Allowed types: PDF, DOC, DOCX, XLS, XLSX, PPT, PPTX, ZIP, TXT, images"
            );
        }
    }

    /**
     * Helper: Safe content type extraction
     */
    private String safeContentType(String contentType) {
        return (contentType == null || contentType.isBlank()) 
            ? MediaType.APPLICATION_OCTET_STREAM_VALUE 
            : contentType;
    }

    /**
     * Helper: Extract filename from storage path
     */
    private String extractFilename(String storagePath) {
        if (storagePath == null) return "unknown";
        int idx = storagePath.lastIndexOf('/');
        return idx >= 0 ? storagePath.substring(idx + 1) : storagePath;
    }

    /**
     * Helper: Convert entity to response DTO
     */
    private FileResponse toResponse(ProjectFile file) {
        return new FileResponse(
            file.getId(),
            file.getProjectId(),
            file.getFilename(),
            file.getOriginalFilename(),
            file.getContentType(),
            file.getFileSize(),
            file.getStoragePath(),
            file.getUploadedByUserId(),
            file.getUploadedAt()
        );
    }
}

