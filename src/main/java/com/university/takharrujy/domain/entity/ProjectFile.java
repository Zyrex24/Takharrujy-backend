package com.university.takharrujy.domain.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * ProjectFile Entity
 * Represents a file uploaded to a project with metadata and storage information
 */
@Entity
@Table(name = "project_files",
       indexes = {
         @Index(name = "idx_project_files_project", columnList = "projectId"),
         @Index(name = "idx_project_files_uploaded_by", columnList = "uploadedByUserId")
       })
public class ProjectFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 150)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false, length = 512)
    private String storagePath;

    @Column(nullable = false)
    private Long uploadedByUserId;

    @Column(nullable = false, updatable = false)
    private Instant uploadedAt = Instant.now();

    // Constructors
    public ProjectFile() {}

    public ProjectFile(Long projectId, String filename, String originalFilename, 
                      String contentType, Long fileSize, String storagePath, 
                      Long uploadedByUserId) {
        this.projectId = projectId;
        this.filename = filename;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.storagePath = storagePath;
        this.uploadedByUserId = uploadedByUserId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
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

    public String getStoragePath() {
        return storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public Long getUploadedByUserId() {
        return uploadedByUserId;
    }

    public void setUploadedByUserId(Long uploadedByUserId) {
        this.uploadedByUserId = uploadedByUserId;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }

    @Override
    public String toString() {
        return "ProjectFile{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", filename='" + filename + '\'' +
                ", originalFilename='" + originalFilename + '\'' +
                ", contentType='" + contentType + '\'' +
                ", fileSize=" + fileSize +
                ", uploadedByUserId=" + uploadedByUserId +
                ", uploadedAt=" + uploadedAt +
                '}';
    }
}

