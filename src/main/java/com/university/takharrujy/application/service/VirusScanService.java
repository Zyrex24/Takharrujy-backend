package com.university.takharrujy.application.service;

import com.university.takharrujy.infrastructure.exception.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Virus Scan Service
 * Handles virus scanning for uploaded files
 * This is a basic implementation - in production, integrate with proper antivirus solution
 */
@Service
public class VirusScanService {
    
    @Value("${app.security.virus-scan.enabled:true}")
    private boolean virusScanEnabled;
    
    // Known malicious file signatures (basic implementation)
    private static final List<byte[]> MALICIOUS_SIGNATURES = Arrays.asList(
        // Example virus signatures - in production use real antivirus engine
        new byte[]{0x4D, 0x5A}, // PE executable header
        new byte[]{0x7F, 0x45, 0x4C, 0x46}, // ELF header
        new byte[]{0xCA, 0xFE, 0xBA, 0xBE} // Java class file
    );
    
    // Dangerous file extensions
    private static final List<String> DANGEROUS_EXTENSIONS = Arrays.asList(
        ".exe", ".bat", ".cmd", ".scr", ".pif", ".com", ".jar", ".vbs", ".js", 
        ".ps1", ".sh", ".dll", ".sys", ".bin", ".app", ".deb", ".rpm"
    );
    
    /**
     * Scan file for viruses and malicious content
     */
    public boolean scanFile(MultipartFile file) {
        if (!virusScanEnabled) {
            return true; // Skip scanning if disabled
        }
        
        try {
            // Check file extension
            if (hasDangerousExtension(file.getOriginalFilename())) {
                return false;
            }
            
            // Check file content for malicious signatures
            byte[] fileContent = file.getBytes();
            
            if (containsMaliciousSignature(fileContent)) {
                return false;
            }
            
            // Check file size - suspicious if too large for claimed type
            if (isSuspiciousFileSize(file)) {
                return false;
            }
            
            // Additional checks for image files
            if (isImageFile(file.getContentType())) {
                return scanImageFile(file);
            }
            
            return true;
            
        } catch (IOException e) {
            // If we can't read the file, consider it suspicious
            return false;
        }
    }
    
    /**
     * Check if filename has dangerous extension
     */
    private boolean hasDangerousExtension(String filename) {
        if (filename == null) {
            return true;
        }
        
        String lowerFilename = filename.toLowerCase();
        return DANGEROUS_EXTENSIONS.stream()
            .anyMatch(lowerFilename::endsWith);
    }
    
    /**
     * Check if file content contains malicious signatures
     */
    private boolean containsMaliciousSignature(byte[] fileContent) {
        if (fileContent == null || fileContent.length < 4) {
            return false;
        }
        
        for (byte[] signature : MALICIOUS_SIGNATURES) {
            if (containsSignature(fileContent, signature)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if file contains specific byte signature
     */
    private boolean containsSignature(byte[] fileContent, byte[] signature) {
        if (fileContent.length < signature.length) {
            return false;
        }
        
        for (int i = 0; i <= fileContent.length - signature.length; i++) {
            boolean found = true;
            for (int j = 0; j < signature.length; j++) {
                if (fileContent[i + j] != signature[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if file size is suspicious for the claimed type
     */
    private boolean isSuspiciousFileSize(MultipartFile file) {
        String contentType = file.getContentType();
        long fileSize = file.getSize();
        
        if (contentType != null && contentType.startsWith("image/")) {
            // Image files shouldn't be larger than 50MB
            return fileSize > 50 * 1024 * 1024;
        }
        
        // General file size limit - 100MB
        return fileSize > 100 * 1024 * 1024;
    }
    
    /**
     * Check if file is an image file
     */
    private boolean isImageFile(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }
    
    /**
     * Scan image file for embedded malicious content
     */
    private boolean scanImageFile(MultipartFile file) {
        try {
            byte[] fileContent = file.getBytes();
            
            // Check for embedded executables in image files
            // Look for PE header embedded in image
            if (containsEmbeddedExecutable(fileContent)) {
                return false;
            }
            
            // Check for suspicious metadata or EXIF data
            if (hasSuspiciousMetadata(fileContent)) {
                return false;
            }
            
            return true;
            
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * Check for embedded executables in file content
     */
    private boolean containsEmbeddedExecutable(byte[] content) {
        // Simple check for PE header beyond the beginning of file
        byte[] peHeader = {0x4D, 0x5A};
        
        // Skip first 100 bytes (normal image headers)
        for (int i = 100; i < content.length - 1; i++) {
            if (content[i] == peHeader[0] && content[i + 1] == peHeader[1]) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check for suspicious metadata in file
     */
    private boolean hasSuspiciousMetadata(byte[] content) {
        // Convert to string and check for suspicious patterns
        String contentString = new String(content);
        
        // Check for script tags or suspicious keywords
        String[] suspiciousPatterns = {
            "<script", "javascript:", "eval(", "document.write",
            "cmd.exe", "powershell", "system("
        };
        
        String lowerContent = contentString.toLowerCase();
        for (String pattern : suspiciousPatterns) {
            if (lowerContent.contains(pattern)) {
                return true;
            }
        }
        
        return false;
    }
}