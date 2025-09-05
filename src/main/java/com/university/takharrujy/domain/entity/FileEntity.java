package com.university.takharrujy.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * File Entity
 * Represents uploaded files in the system
 */
@Entity
@Table(name = "files")
public class FileEntity extends BaseEntity {

    @NotBlank(message = "Original filename is required")
    @Size(max = 255, message = "Original filename cannot exceed 255 characters")
    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @NotBlank(message = "Unique filename is required")
    @Size(max = 255, message = "Unique filename cannot exceed 255 characters")
    @Column(name = "unique_filename", nullable = false, unique = true)
    private String uniqueFilename;

    @Size(max = 100, message = "Content type cannot exceed 100 characters")
    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Size(max = 1000, message = "Storage URL cannot exceed 1000 characters")
    @Column(name = "storage_url", nullable = false)
    private String storageUrl;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Column(name = "description")
    private String description;

    @Size(max = 500, message = "Arabic description cannot exceed 500 characters")
    @Column(name = "description_ar")
    private String descriptionAr;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "download_count")
    private Long downloadCount = 0L;

    @Size(max = 50, message = "File category cannot exceed 50 characters")
    @Column(name = "file_category")
    private String fileCategory;

    @Size(max = 50, message = "Virus scan status cannot exceed 50 characters")
    @Column(name = "virus_scan_status")
    private String virusScanStatus = "PENDING";

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Size(max = 32, message = "File hash cannot exceed 32 characters")
    @Column(name = "file_hash")
    private String fileHash;

    @Column(name = "file_version")
    private Integer fileVersion = 1;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_file_id")
    private FileEntity parentFile;

    // Constructors
    public FileEntity() {
        super();
    }

    public FileEntity(String originalFilename, String uniqueFilename, Long fileSize, 
                     String storageUrl, User uploadedBy, Long universityId) {
        super(universityId);
        this.originalFilename = originalFilename;
        this.uniqueFilename = uniqueFilename;
        this.fileSize = fileSize;
        this.storageUrl = storageUrl;
        this.uploadedBy = uploadedBy;
    }

    // Business methods
    public void incrementDownloadCount() {
        this.downloadCount++;
    }

    public boolean isImage() {
        return contentType != null && contentType.startsWith("image/");
    }

    public boolean isDocument() {
        return contentType != null && (
            contentType.contains("pdf") ||
            contentType.contains("document") ||
            contentType.contains("text") ||
            contentType.contains("spreadsheet")
        );
    }

    public boolean isVideo() {
        return contentType != null && contentType.startsWith("video/");
    }

    public boolean isVirusScanClean() {
        return "CLEAN".equals(virusScanStatus);
    }

    public String getFileExtension() {
        if (originalFilename != null && originalFilename.contains(".")) {
            return originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        }
        return "";
    }

    public String getFileSizeFormatted() {
        if (fileSize == null) return "0 B";
        
        long bytes = fileSize;
        if (bytes < 1024) return bytes + " B";
        
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    // Getters and Setters
    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getUniqueFilename() {
        return uniqueFilename;
    }

    public void setUniqueFilename(String uniqueFilename) {
        this.uniqueFilename = uniqueFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getStorageUrl() {
        return storageUrl;
    }

    public void setStorageUrl(String storageUrl) {
        this.storageUrl = storageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionAr() {
        return descriptionAr;
    }

    public void setDescriptionAr(String descriptionAr) {
        this.descriptionAr = descriptionAr;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Long getDownloadCount() {
        return downloadCount;
    }

    public void setDownloadCount(Long downloadCount) {
        this.downloadCount = downloadCount;
    }

    public String getFileCategory() {
        return fileCategory;
    }

    public void setFileCategory(String fileCategory) {
        this.fileCategory = fileCategory;
    }

    public String getVirusScanStatus() {
        return virusScanStatus;
    }

    public void setVirusScanStatus(String virusScanStatus) {
        this.virusScanStatus = virusScanStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Integer getFileVersion() {
        return fileVersion;
    }

    public void setFileVersion(Integer fileVersion) {
        this.fileVersion = fileVersion;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public User getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(User uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public FileEntity getParentFile() {
        return parentFile;
    }

    public void setParentFile(FileEntity parentFile) {
        this.parentFile = parentFile;
    }
}