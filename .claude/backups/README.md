# Takharrujy Platform - Memory Backup System

## Overview
Automated backup system for memory files with date-based organization and retention policies.

## Backup Structure
```
.claude/backups/
├── backup-script.sh          # Linux/Mac backup script
├── backup-script.ps1         # Windows PowerShell backup script
├── README.md                 # This file
└── YYYY-MM-DD/              # Daily backup directories
    ├── backup-manifest.json  # Backup metadata and verification
    ├── backup-report.txt     # Human-readable backup report
    ├── memory.json.backup    # Core memory data store
    ├── config.json.backup    # Configuration backup
    ├── README.md.backup      # Documentation backup
    ├── context/              # Context files backup
    │   ├── knowledge.md.backup
    │   ├── patterns.md.backup
    │   ├── decisions.md.backup
    │   └── tasks.md.backup
    ├── commands/             # Command files backup
    ├── agents/               # Agent files backup
    └── workflows/            # Workflow files backup
```

## Usage

### Windows PowerShell
```powershell
# Run backup from project root
powershell -ExecutionPolicy Bypass -File .claude/backups/backup-script.ps1

# Run with custom retention (default 30 days)
powershell -ExecutionPolicy Bypass -File .claude/backups/backup-script.ps1 -RetentionDays 60

# Run with verbose output
powershell -ExecutionPolicy Bypass -File .claude/backups/backup-script.ps1 -Verbose
```

### Linux/Mac Bash
```bash
# Run backup from project root
./.claude/backups/backup-script.sh

# Run with custom retention
RETENTION_DAYS=60 ./.claude/backups/backup-script.sh
```

## Automated Scheduling

### Windows Task Scheduler
1. Open Task Scheduler
2. Create Basic Task
3. Set trigger to Daily at preferred time
4. Set action to start program:
   - Program: `powershell`
   - Arguments: `-ExecutionPolicy Bypass -File "C:\path\to\project\.claude\backups\backup-script.ps1"`
   - Start in: `C:\path\to\project`

### Linux/Mac Cron
```bash
# Edit crontab
crontab -e

# Add daily backup at 2 AM
0 2 * * * cd /path/to/project && ./.claude/backups/backup-script.sh

# Add weekly cleanup
0 3 * * 0 cd /path/to/project && ./.claude/backups/backup-script.sh
```

## Backup Features

### Core Memory Files
- `memory.json` - Project memory data store
- `config.json` - Configuration settings
- `README.md` - Documentation

### Context Files
- `knowledge.md` - Domain expertise and technical knowledge
- `patterns.md` - Coding patterns and architectural decisions
- `decisions.md` - Decision history and rationale
- `tasks.md` - Task tracking and sprint progress

### Command and Agent Files
- All `.md` files in `commands/` directory
- All `.md` files in `agents/` directory
- All `.md` files in `workflows/` directory

### Backup Verification
- File existence checks
- Size validation
- Integrity verification
- Backup manifest creation

### Retention Management
- Automatic cleanup of old backups
- Configurable retention period (default: 30 days)
- Disk space optimization

## Restoration

### Manual Restoration
```powershell
# Windows - Restore from specific backup
$BackupDate = "2024-12-01"
Copy-Item ".claude\backups\$BackupDate\memory.json.backup" ".claude\memory.json"
Copy-Item ".claude\backups\$BackupDate\context\*.backup" ".claude\context\" -Recurse

# Remove .backup extension
Get-ChildItem ".claude\context\*.backup" | Rename-Item -NewName {$_.Name -replace '\.backup$',''}
```

```bash
# Linux/Mac - Restore from specific backup
BACKUP_DATE="2024-12-01"
cp ".claude/backups/$BACKUP_DATE/memory.json.backup" ".claude/memory.json"
cp -r ".claude/backups/$BACKUP_DATE/context/"*.backup ".claude/context/"

# Remove .backup extension
for file in .claude/context/*.backup; do
    mv "$file" "${file%.backup}"
done
```

### Automated Restoration Script
Create restoration script for emergency recovery:

```powershell
# restore-memory.ps1
param([string]$BackupDate = (Get-Date).AddDays(-1).ToString("yyyy-MM-dd"))

Write-Host "Restoring memory from backup: $BackupDate"
$BackupDir = ".claude\backups\$BackupDate"

if (Test-Path $BackupDir) {
    # Restore core files
    Copy-Item "$BackupDir\memory.json.backup" ".claude\memory.json" -Force
    Copy-Item "$BackupDir\config.json.backup" ".claude\config.json" -Force
    
    # Restore context files
    Get-ChildItem "$BackupDir\context\*.backup" | ForEach-Object {
        $DestName = $_.Name -replace '\.backup$',''
        Copy-Item $_.FullName ".claude\context\$DestName" -Force
    }
    
    Write-Host "Memory restoration completed successfully!"
} else {
    Write-Error "Backup not found: $BackupDir"
}
```

## Monitoring and Alerts

### Backup Success Monitoring
- Check backup manifest for completion status
- Verify backup size and file count
- Monitor backup script exit codes

### Alert Configuration
```powershell
# Add to backup script for email alerts
if ($BackupFailed) {
    Send-MailMessage -To "admin@university.edu" -Subject "Takharrujy Backup Failed" -Body "Backup failed on $(Get-Date)"
}
```

### Health Checks
```bash
# Check last backup age
LAST_BACKUP=$(ls -1 .claude/backups/ | tail -1)
BACKUP_AGE=$(( ($(date +%s) - $(date -d "$LAST_BACKUP" +%s)) / 86400 ))

if [ $BACKUP_AGE -gt 1 ]; then
    echo "WARNING: Last backup is $BACKUP_AGE days old"
fi
```

## Troubleshooting

### Common Issues

1. **Permission Denied**
   - Ensure script has execute permissions
   - Run with appropriate user privileges

2. **Disk Space Full**
   - Check available disk space
   - Reduce retention period
   - Manual cleanup of old backups

3. **Missing Files**
   - Verify source files exist
   - Check file paths in script
   - Review backup manifest

4. **Backup Size Too Small**
   - Verify all source files are present
   - Check for empty or corrupted files
   - Review backup script logs

### Debug Mode
```powershell
# Windows debug
$DebugPreference = "Continue"
powershell -ExecutionPolicy Bypass -File .claude/backups/backup-script.ps1 -Verbose
```

```bash
# Linux/Mac debug
bash -x ./.claude/backups/backup-script.sh
```

## Security Considerations

### File Permissions
- Backup files should have restricted access
- Use appropriate file system permissions
- Consider encryption for sensitive data

### Access Control
- Limit backup script execution to authorized users
- Secure backup storage location
- Regular security audits of backup procedures

### Data Privacy
- Backup files may contain sensitive project information
- Follow institutional data handling policies
- Consider data anonymization for development backups

---

**Backup System Status:** ✅ Implemented and Ready  
**Retention Policy:** 30 days (configurable)  
**Backup Frequency:** Daily (recommended)  
**Verification:** Automatic integrity checks  
**Platform Support:** Windows PowerShell, Linux/Mac Bash  
**Last Updated:** December 2024

This backup system ensures reliable preservation of the Takharrujy platform's memory and knowledge base, providing disaster recovery and data continuity capabilities.
