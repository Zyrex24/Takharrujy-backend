package com.university.takharrujy.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VirusScanService
 * Tests virus scanning functionality for uploaded files
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Virus Scan Service Tests")
class VirusScanServiceTest {

    @InjectMocks
    private VirusScanService virusScanService;

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
        // Enable virus scanning by default
        ReflectionTestUtils.setField(virusScanService, "virusScanEnabled", true);
    }

    @Test
    @DisplayName("Should return true for safe file when virus scan is enabled")
    void shouldReturnTrueForSafeFile() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("Safe PDF content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true when virus scan is disabled")
    void shouldReturnTrueWhenVirusScanDisabled() throws IOException {
        // Given
        ReflectionTestUtils.setField(virusScanService, "virusScanEnabled", false);
        mockFile = mock(MultipartFile.class);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
        verifyNoInteractions(mockFile);
    }

    @Test
    @DisplayName("Should return false for file with dangerous extension")
    void shouldReturnFalseForDangerousExtension() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("malware.exe");
        when(mockFile.getContentType()).thenReturn("application/octet-stream");
        when(mockFile.getSize()).thenReturn(1024L);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for file with null filename")
    void shouldReturnFalseForNullFilename() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn(null);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(1024L);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for file with malicious signature")
    void shouldReturnFalseForMaliciousSignature() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(mockFile.getSize()).thenReturn(1024L);
        // Create content with PE header signature (0x4D, 0x5A)
        byte[] maliciousContent = new byte[]{(byte) 0x4D, (byte) 0x5A, (byte) 0x90, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};
        when(mockFile.getBytes()).thenReturn(maliciousContent);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for suspiciously large image file")
    void shouldReturnFalseForSuspiciouslyLargeImageFile() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(60L * 1024 * 1024); // 60MB - exceeds 50MB limit
        when(mockFile.getBytes()).thenReturn("Large image content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for suspiciously large general file")
    void shouldReturnFalseForSuspiciouslyLargeGeneralFile() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(110L * 1024 * 1024); // 110MB - exceeds 100MB limit
        when(mockFile.getBytes()).thenReturn("Large PDF content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for image with embedded executable")
    void shouldReturnFalseForImageWithEmbeddedExecutable() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);
        
        // Create content with PE header embedded after position 100
        byte[] content = new byte[200];
        // Fill with normal image data
        for (int i = 0; i < 100; i++) {
            content[i] = (byte) 0xFF;
        }
        // Add PE header at position 100
        content[100] = 0x4D;
        content[101] = 0x5A;
        when(mockFile.getBytes()).thenReturn(content);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false for image with suspicious metadata")
    void shouldReturnFalseForImageWithSuspiciousMetadata() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);
        
        // Create content with suspicious script tag
        String suspiciousContent = "Normal image data <script>alert('xss')</script> more data";
        when(mockFile.getBytes()).thenReturn(suspiciousContent.getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return false when IOException occurs during file reading")
    void shouldReturnFalseWhenIOExceptionOccurs() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenThrow(new IOException("Cannot read file"));

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should return true for safe image file")
    void shouldReturnTrueForSafeImageFile() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("Safe image content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for file with safe extension")
    void shouldReturnTrueForSafeExtension() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.pdf");
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("Safe PDF content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return true for file with safe content")
    void shouldReturnTrueForSafeContent() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("Safe text content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should handle various dangerous extensions")
    void shouldHandleVariousDangerousExtensions() throws IOException {
        // Test multiple dangerous extensions
        String[] dangerousExtensions = {
            "malware.exe", "script.bat", "virus.cmd", "trojan.scr", 
            "backdoor.pif", "malware.com", "exploit.jar", "virus.vbs",
            "script.js", "powershell.ps1", "shell.sh", "malware.dll",
            "driver.sys", "binary.bin", "app.app", "package.deb", "package.rpm"
        };

        for (String filename : dangerousExtensions) {
            // Given
            mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(filename);
            when(mockFile.getContentType()).thenReturn("application/octet-stream");
            when(mockFile.getSize()).thenReturn(1024L);

            // When
            boolean result = virusScanService.scanFile(mockFile);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("Should handle various malicious signatures")
    void shouldHandleVariousMaliciousSignatures() throws IOException {
        // Test PE header signature
        testMaliciousSignature(new byte[]{0x4D, 0x5A});
        
        // Test ELF header signature
        testMaliciousSignature(new byte[]{0x7F, 0x45, 0x4C, 0x46});
        
        // Test Java class file signature
        testMaliciousSignature(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE});
    }

    private void testMaliciousSignature(byte[] signature) throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("document.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn(signature);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle suspicious metadata patterns")
    void shouldHandleSuspiciousMetadataPatterns() throws IOException {
        String[] suspiciousPatterns = {
            "<script>alert('xss')</script>",
            "javascript:alert('xss')",
            "eval(malicious_code)",
            "document.write('xss')",
            "cmd.exe /c malicious_command",
            "powershell -Command malicious_script",
            "system('rm -rf /')"
        };

        for (String pattern : suspiciousPatterns) {
            // Given
            mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
            when(mockFile.getContentType()).thenReturn("image/jpeg");
            when(mockFile.getSize()).thenReturn(1024L);
            when(mockFile.getBytes()).thenReturn(("Normal content " + pattern + " more content").getBytes());

            // When
            boolean result = virusScanService.scanFile(mockFile);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("Should handle case-insensitive dangerous extensions")
    void shouldHandleCaseInsensitiveDangerousExtensions() throws IOException {
        String[] caseVariations = {
            "MALWARE.EXE", "script.BAT", "Virus.Cmd", "TROJAN.SCR"
        };

        for (String filename : caseVariations) {
            // Given
            mockFile = mock(MultipartFile.class);
            when(mockFile.getOriginalFilename()).thenReturn(filename);
            when(mockFile.getContentType()).thenReturn("application/octet-stream");
            when(mockFile.getSize()).thenReturn(1024L);

            // When
            boolean result = virusScanService.scanFile(mockFile);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Test
    @DisplayName("Should handle case-insensitive suspicious metadata")
    void shouldHandleCaseInsensitiveSuspiciousMetadata() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("image.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getSize()).thenReturn(1024L);
        when(mockFile.getBytes()).thenReturn("Normal content <SCRIPT>alert('xss')</SCRIPT> more content".getBytes());

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle empty file content")
    void shouldHandleEmptyFileContent() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("empty.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(mockFile.getSize()).thenReturn(0L);
        when(mockFile.getBytes()).thenReturn(new byte[0]);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should handle null file content")
    void shouldHandleNullFileContent() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("null.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(mockFile.getSize()).thenReturn(0L);
        when(mockFile.getBytes()).thenReturn(null);

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should handle very small file content")
    void shouldHandleVerySmallFileContent() throws IOException {
        // Given
        mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("tiny.txt");
        when(mockFile.getContentType()).thenReturn("text/plain");
        when(mockFile.getSize()).thenReturn(2L);
        when(mockFile.getBytes()).thenReturn(new byte[]{0x4D, 0x5A}); // PE header but too small

        // When
        boolean result = virusScanService.scanFile(mockFile);

        // Then
        assertThat(result).isTrue(); // Should pass because content is too small for signature check
    }
}
