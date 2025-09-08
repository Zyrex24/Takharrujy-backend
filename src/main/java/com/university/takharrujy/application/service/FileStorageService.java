package com.university.takharrujy.application.service;

import com.university.takharrujy.infrastructure.exception.FileStorageException;
import com.university.takharrujy.infrastructure.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * File Storage Service
 * Handles file storage operations including avatar uploads with virus scanning
 */
@Service
public class FileStorageService {
    
    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${app.file.avatar-dir:avatars}")
    private String avatarDir;
    
    @Value("${app.file.max-avatar-size:5242880}") // 5MB
    private long maxAvatarSize;
    
    @Value("${app.file.avatar-dimensions:300}")
    private int avatarDimensions;
    
    private final VirusScanService virusScanService;
    
    public FileStorageService(VirusScanService virusScanService) {
        this.virusScanService = virusScanService;
    }
    
    /**
     * Upload and process user avatar
     */
    public String uploadAvatar(MultipartFile file, Long userId) {
        try {
            // Validate file
            validateAvatarFile(file);
            
            // Perform virus scan
            if (!virusScanService.scanFile(file)) {
                throw new ValidationException("File failed security scan");
            }
            
            // Create directory if not exists
            Path avatarDirPath = Paths.get(uploadDir, avatarDir);
            Files.createDirectories(avatarDirPath);
            
            // Generate unique filename
            String fileExtension = getFileExtension(file.getOriginalFilename());
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = String.format("avatar_%d_%s_%s%s", 
                userId, timestamp, UUID.randomUUID().toString().substring(0, 8), fileExtension);
            
            // Process and resize image
            byte[] processedImageData = processAvatarImage(file, avatarDimensions);
            
            // Save processed file
            Path filePath = avatarDirPath.resolve(filename);
            Files.write(filePath, processedImageData);
            
            // Return relative URL path
            return String.format("/%s/%s/%s", uploadDir, avatarDir, filename);
            
        } catch (IOException e) {
            throw new FileStorageException("Failed to store avatar file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete avatar file
     */
    public void deleteAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty()) {
            return;
        }
        
        try {
            // Extract filename from URL
            String filename = avatarUrl.substring(avatarUrl.lastIndexOf('/') + 1);
            Path filePath = Paths.get(uploadDir, avatarDir, filename);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Failed to delete avatar file: " + e.getMessage());
        }
    }
    
    /**
     * Validate avatar file
     */
    private void validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("Avatar file is required");
        }
        
        if (file.getSize() > maxAvatarSize) {
            throw new ValidationException("Avatar file size cannot exceed 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("Avatar must be an image file");
        }
        
        if (!contentType.equals("image/jpeg") && 
            !contentType.equals("image/png") && 
            !contentType.equals("image/webp")) {
            throw new ValidationException("Avatar must be in JPEG, PNG, or WebP format");
        }
    }
    
    /**
     * Process avatar image - resize and optimize
     */
    private byte[] processAvatarImage(MultipartFile file, int targetSize) throws IOException {
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        
        if (originalImage == null) {
            throw new ValidationException("Invalid image file");
        }
        
        // Calculate dimensions maintaining aspect ratio
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        int newWidth, newHeight;
        
        if (originalWidth > originalHeight) {
            newWidth = targetSize;
            newHeight = (targetSize * originalHeight) / originalWidth;
        } else {
            newHeight = targetSize;
            newWidth = (targetSize * originalWidth) / originalHeight;
        }
        
        // Create resized image
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = resizedImage.createGraphics();
        
        // Enable high-quality rendering
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw resized image
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        // Convert to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedImage, "jpg", baos);
        
        return baos.toByteArray();
    }
    
    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // default extension
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}