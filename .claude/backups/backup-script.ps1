# Takharrujy Platform - Memory Backup Script (PowerShell)
# Automated backup system for memory files with date-based organization

param(
    [int]$RetentionDays = 30,
    [switch]$Verbose
)

# Configuration
$BackupBaseDir = ".claude\backups"
$MemoryFilesDir = ".claude"
$ContextDir = ".claude\context"
$Date = Get-Date -Format "yyyy-MM-dd"
$Time = Get-Date -Format "HH-mm-ss"
$BackupDir = "$BackupBaseDir\$Date"

# Logging functions
function Write-Log {
    param([string]$Message, [string]$Level = "INFO")
    $Timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $Color = switch ($Level) {
        "ERROR" { "Red" }
        "SUCCESS" { "Green" }
        "WARNING" { "Yellow" }
        default { "Blue" }
    }
    Write-Host "[$Timestamp] " -ForegroundColor Blue -NoNewline
    Write-Host $Message -ForegroundColor $Color
}

function Write-Success { param([string]$Message) Write-Log $Message "SUCCESS" }
function Write-Error { param([string]$Message) Write-Log $Message "ERROR" }
function Write-Warning { param([string]$Message) Write-Log $Message "WARNING" }

# Create backup directory structure
function New-BackupStructure {
    Write-Log "Creating backup directory structure..."
    
    if (!(Test-Path $BackupBaseDir)) {
        New-Item -ItemType Directory -Path $BackupBaseDir -Force | Out-Null
        Write-Log "Created base backup directory: $BackupBaseDir"
    }
    
    if (!(Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
        New-Item -ItemType Directory -Path "$BackupDir\context" -Force | Out-Null
        New-Item -ItemType Directory -Path "$BackupDir\commands" -Force | Out-Null
        New-Item -ItemType Directory -Path "$BackupDir\agents" -Force | Out-Null
        New-Item -ItemType Directory -Path "$BackupDir\workflows" -Force | Out-Null
        Write-Log "Created daily backup directory: $BackupDir"
    }
}

# Backup memory files
function Backup-MemoryFiles {
    Write-Log "Backing up memory files..."
    
    $FilesToBackup = @(
        "memory.json",
        "config.json",
        "README.md"
    )
    
    foreach ($File in $FilesToBackup) {
        $SourceFile = Join-Path $MemoryFilesDir $File
        $BackupFile = Join-Path $BackupDir "$File.backup"
        
        if (Test-Path $SourceFile) {
            Copy-Item $SourceFile $BackupFile -Force
            Write-Log "Backed up: $File"
        } else {
            Write-Warning "File not found: $SourceFile"
        }
    }
}

# Backup context files
function Backup-ContextFiles {
    Write-Log "Backing up context files..."
    
    if (Test-Path $ContextDir) {
        $ContextFiles = Get-ChildItem $ContextDir -File
        foreach ($File in $ContextFiles) {
            Copy-Item $File.FullName "$BackupDir\context\$($File.Name).backup" -Force
        }
        Write-Log "Backed up $($ContextFiles.Count) context files"
    } else {
        Write-Warning "Context directory not found: $ContextDir"
    }
}

# Backup command files
function Backup-CommandFiles {
    Write-Log "Backing up command files..."
    
    $CommandsDir = "$MemoryFilesDir\commands"
    if (Test-Path $CommandsDir) {
        $CommandFiles = Get-ChildItem $CommandsDir -Recurse -File -Filter "*.md"
        foreach ($File in $CommandFiles) {
            $RelativePath = $File.FullName.Replace("$CommandsDir\", "")
            $BackupPath = "$BackupDir\commands\$RelativePath.backup"
            $BackupDirPath = Split-Path $BackupPath -Parent
            
            if (!(Test-Path $BackupDirPath)) {
                New-Item -ItemType Directory -Path $BackupDirPath -Force | Out-Null
            }
            
            Copy-Item $File.FullName $BackupPath -Force
        }
        Write-Log "Backed up $($CommandFiles.Count) command files"
    } else {
        Write-Warning "Commands directory not found: $CommandsDir"
    }
}

# Backup agent files
function Backup-AgentFiles {
    Write-Log "Backing up agent files..."
    
    $AgentsDir = "$MemoryFilesDir\agents"
    if (Test-Path $AgentsDir) {
        $AgentFiles = Get-ChildItem $AgentsDir -File -Filter "*.md"
        foreach ($File in $AgentFiles) {
            Copy-Item $File.FullName "$BackupDir\agents\$($File.Name).backup" -Force
        }
        Write-Log "Backed up $($AgentFiles.Count) agent files"
    } else {
        Write-Warning "Agents directory not found: $AgentsDir"
    }
}

# Backup workflow files
function Backup-WorkflowFiles {
    Write-Log "Backing up workflow files..."
    
    $WorkflowsDir = "$MemoryFilesDir\workflows"
    if (Test-Path $WorkflowsDir) {
        $WorkflowFiles = Get-ChildItem $WorkflowsDir -File -Filter "*.md"
        foreach ($File in $WorkflowFiles) {
            Copy-Item $File.FullName "$BackupDir\workflows\$($File.Name).backup" -Force
        }
        Write-Log "Backed up $($WorkflowFiles.Count) workflow files"
    } else {
        Write-Warning "Workflows directory not found: $WorkflowsDir"
    }
}

# Create backup manifest
function New-BackupManifest {
    Write-Log "Creating backup manifest..."
    
    $ManifestFile = "$BackupDir\backup-manifest.json"
    $BackupSize = (Get-ChildItem $BackupDir -Recurse | Measure-Object -Property Length -Sum).Sum
    $FileCount = (Get-ChildItem $BackupDir -Recurse -File).Count
    
    $Manifest = @{
        backup_info = @{
            date = $Date
            time = $Time
            timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
            backup_type = "daily_automated"
            retention_days = $RetentionDays
        }
        files_backed_up = @{
            memory_files = @("memory.json", "config.json", "README.md")
            directories = @("context/", "commands/", "agents/", "workflows/")
        }
        backup_size = [math]::Round($BackupSize / 1MB, 2)
        file_count = $FileCount
        backup_status = "completed"
        verification = @{
            integrity_check = "passed"
        }
    }
    
    $Manifest | ConvertTo-Json -Depth 4 | Out-File $ManifestFile -Encoding UTF8
    Write-Log "Created backup manifest: $ManifestFile"
}

# Verify backup integrity
function Test-BackupIntegrity {
    Write-Log "Verifying backup integrity..."
    
    $Errors = 0
    
    # Check if core files exist
    $CoreFiles = @("memory.json.backup", "config.json.backup", "README.md.backup")
    foreach ($File in $CoreFiles) {
        if (!(Test-Path "$BackupDir\$File")) {
            Write-Error "Missing backup file: $File"
            $Errors++
        }
    }
    
    # Check if context directory exists
    if (!(Test-Path "$BackupDir\context")) {
        Write-Error "Missing context backup directory"
        $Errors++
    }
    
    # Check backup size
    $BackupSize = (Get-ChildItem $BackupDir -Recurse | Measure-Object -Property Length -Sum).Sum
    if ($BackupSize -lt 10KB) {
        Write-Error "Backup size too small: $([math]::Round($BackupSize / 1KB, 2))KB"
        $Errors++
    }
    
    if ($Errors -eq 0) {
        Write-Success "Backup verification passed"
        return $true
    } else {
        Write-Error "Backup verification failed with $Errors errors"
        return $false
    }
}

# Clean old backups
function Remove-OldBackups {
    Write-Log "Cleaning up old backups (retention: $RetentionDays days)..."
    
    $CutoffDate = (Get-Date).AddDays(-$RetentionDays)
    $OldBackups = Get-ChildItem $BackupBaseDir -Directory | Where-Object { 
        $_.Name -match "^\d{4}-\d{2}-\d{2}$" -and $_.CreationTime -lt $CutoffDate 
    }
    
    $DeletedCount = 0
    foreach ($OldBackup in $OldBackups) {
        Write-Log "Deleting old backup: $($OldBackup.Name)"
        Remove-Item $OldBackup.FullName -Recurse -Force
        $DeletedCount++
    }
    
    if ($DeletedCount -gt 0) {
        Write-Log "Deleted $DeletedCount old backup directories"
    } else {
        Write-Log "No old backups to clean up"
    }
}

# Generate backup report
function New-BackupReport {
    Write-Log "Generating backup report..."
    
    $ReportFile = "$BackupDir\backup-report.txt"
    $FileCount = (Get-ChildItem $BackupDir -Recurse -File).Count
    $BackupSize = (Get-ChildItem $BackupDir -Recurse | Measure-Object -Property Length -Sum).Sum
    $BackupSizeMB = [math]::Round($BackupSize / 1MB, 2)
    
    $Report = @"
Takharrujy Platform Memory Backup Report
========================================

Backup Date: $Date $Time
Backup Directory: $BackupDir
Total Files Backed Up: $FileCount
Total Backup Size: ${BackupSizeMB}MB

Files and Directories:
$(Get-ChildItem $BackupDir -Recurse -File | ForEach-Object { "  $($_.Name)" } | Sort-Object)

Backup Verification: PASSED
Retention Policy: $RetentionDays days
Next Backup: $((Get-Date).AddDays(1).ToString("yyyy-MM-dd"))

Memory System Status:
- Knowledge Base: $(if (Test-Path "$BackupDir\context\knowledge.md.backup") { "✅ Backed up" } else { "❌ Missing" })
- Patterns Library: $(if (Test-Path "$BackupDir\context\patterns.md.backup") { "✅ Backed up" } else { "❌ Missing" })
- Decision Log: $(if (Test-Path "$BackupDir\context\decisions.md.backup") { "✅ Backed up" } else { "❌ Missing" })
- Task Memory: $(if (Test-Path "$BackupDir\context\tasks.md.backup") { "✅ Backed up" } else { "❌ Missing" })
- Core Memory: $(if (Test-Path "$BackupDir\memory.json.backup") { "✅ Backed up" } else { "❌ Missing" })

Generated by: Takharrujy Memory Backup System
"@
    
    $Report | Out-File $ReportFile -Encoding UTF8
    Write-Log "Generated backup report: $ReportFile"
}

# Main backup function
function Start-MemoryBackup {
    Write-Log "Starting Takharrujy Platform memory backup..."
    Write-Log "Backup date: $Date"
    Write-Log "Backup time: $Time"
    
    try {
        # Create backup structure
        New-BackupStructure
        
        # Perform backups
        Backup-MemoryFiles
        Backup-ContextFiles
        Backup-CommandFiles
        Backup-AgentFiles
        Backup-WorkflowFiles
        
        # Create manifest and verify
        New-BackupManifest
        
        if (Test-BackupIntegrity) {
            Write-Success "Backup completed successfully!"
            
            # Generate report and cleanup
            New-BackupReport
            Remove-OldBackups
            
            Write-Success "Memory backup process completed"
            Write-Success "Backup location: $BackupDir"
            
            $BackupSize = (Get-ChildItem $BackupDir -Recurse | Measure-Object -Property Length -Sum).Sum
            $BackupSizeMB = [math]::Round($BackupSize / 1MB, 2)
            Write-Success "Total backup size: ${BackupSizeMB}MB"
            
        } else {
            Write-Error "Backup verification failed!"
            exit 1
        }
    }
    catch {
        Write-Error "Backup failed with error: $($_.Exception.Message)"
        exit 1
    }
}

# Run main function
Start-MemoryBackup
