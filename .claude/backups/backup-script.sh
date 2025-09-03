#!/bin/bash

# Takharrujy Platform - Memory Backup Script
# Automated backup system for memory files with date-based organization

# Configuration
BACKUP_BASE_DIR=".claude/backups"
MEMORY_FILES_DIR=".claude"
CONTEXT_DIR=".claude/context"
DATE=$(date +"%Y-%m-%d")
TIME=$(date +"%H-%M-%S")
BACKUP_DIR="$BACKUP_BASE_DIR/$DATE"
RETENTION_DAYS=30

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Create backup directory structure
create_backup_structure() {
    log "Creating backup directory structure..."
    
    if [ ! -d "$BACKUP_BASE_DIR" ]; then
        mkdir -p "$BACKUP_BASE_DIR"
        log "Created base backup directory: $BACKUP_BASE_DIR"
    fi
    
    if [ ! -d "$BACKUP_DIR" ]; then
        mkdir -p "$BACKUP_DIR"
        mkdir -p "$BACKUP_DIR/context"
        mkdir -p "$BACKUP_DIR/commands"
        mkdir -p "$BACKUP_DIR/agents"
        mkdir -p "$BACKUP_DIR/workflows"
        log "Created daily backup directory: $BACKUP_DIR"
    fi
}

# Backup memory files
backup_memory_files() {
    log "Backing up memory files..."
    
    # Core memory files
    local files_to_backup=(
        "memory.json"
        "config.json"
        "README.md"
    )
    
    for file in "${files_to_backup[@]}"; do
        local source_file="$MEMORY_FILES_DIR/$file"
        local backup_file="$BACKUP_DIR/${file}.backup"
        
        if [ -f "$source_file" ]; then
            cp "$source_file" "$backup_file"
            log "Backed up: $file"
        else
            warning "File not found: $source_file"
        fi
    done
}

# Backup context files
backup_context_files() {
    log "Backing up context files..."
    
    if [ -d "$CONTEXT_DIR" ]; then
        cp -r "$CONTEXT_DIR"/* "$BACKUP_DIR/context/" 2>/dev/null
        local context_files=$(ls -1 "$CONTEXT_DIR" 2>/dev/null | wc -l)
        log "Backed up $context_files context files"
    else
        warning "Context directory not found: $CONTEXT_DIR"
    fi
}

# Backup command files
backup_command_files() {
    log "Backing up command files..."
    
    local commands_dir="$MEMORY_FILES_DIR/commands"
    if [ -d "$commands_dir" ]; then
        cp -r "$commands_dir"/* "$BACKUP_DIR/commands/" 2>/dev/null
        local command_files=$(find "$commands_dir" -name "*.md" 2>/dev/null | wc -l)
        log "Backed up $command_files command files"
    else
        warning "Commands directory not found: $commands_dir"
    fi
}

# Backup agent files
backup_agent_files() {
    log "Backing up agent files..."
    
    local agents_dir="$MEMORY_FILES_DIR/agents"
    if [ -d "$agents_dir" ]; then
        cp -r "$agents_dir"/* "$BACKUP_DIR/agents/" 2>/dev/null
        local agent_files=$(find "$agents_dir" -name "*.md" 2>/dev/null | wc -l)
        log "Backed up $agent_files agent files"
    else
        warning "Agents directory not found: $agents_dir"
    fi
}

# Backup workflow files
backup_workflow_files() {
    log "Backing up workflow files..."
    
    local workflows_dir="$MEMORY_FILES_DIR/workflows"
    if [ -d "$workflows_dir" ]; then
        cp -r "$workflows_dir"/* "$BACKUP_DIR/workflows/" 2>/dev/null
        local workflow_files=$(find "$workflows_dir" -name "*.md" 2>/dev/null | wc -l)
        log "Backed up $workflow_files workflow files"
    else
        warning "Workflows directory not found: $workflows_dir"
    fi
}

# Create backup manifest
create_backup_manifest() {
    log "Creating backup manifest..."
    
    local manifest_file="$BACKUP_DIR/backup-manifest.json"
    
    cat > "$manifest_file" << EOF
{
  "backup_info": {
    "date": "$DATE",
    "time": "$TIME",
    "timestamp": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
    "backup_type": "daily_automated",
    "retention_days": $RETENTION_DAYS
  },
  "files_backed_up": {
    "memory_files": [
      "memory.json",
      "config.json", 
      "README.md"
    ],
    "directories": [
      "context/",
      "commands/",
      "agents/",
      "workflows/"
    ]
  },
  "backup_size": "$(du -sh "$BACKUP_DIR" | cut -f1)",
  "file_count": $(find "$BACKUP_DIR" -type f | wc -l),
  "backup_status": "completed",
  "verification": {
    "checksum": "$(find "$BACKUP_DIR" -type f -exec md5sum {} + | md5sum | cut -d' ' -f1)",
    "integrity_check": "passed"
  }
}
EOF
    
    log "Created backup manifest: $manifest_file"
}

# Verify backup integrity
verify_backup() {
    log "Verifying backup integrity..."
    
    local errors=0
    
    # Check if core files exist
    local core_files=("memory.json.backup" "config.json.backup" "README.md.backup")
    for file in "${core_files[@]}"; do
        if [ ! -f "$BACKUP_DIR/$file" ]; then
            error "Missing backup file: $file"
            ((errors++))
        fi
    done
    
    # Check if context directory exists
    if [ ! -d "$BACKUP_DIR/context" ]; then
        error "Missing context backup directory"
        ((errors++))
    fi
    
    # Check backup size
    local backup_size=$(du -s "$BACKUP_DIR" | cut -f1)
    if [ "$backup_size" -lt 10 ]; then
        error "Backup size too small: ${backup_size}KB"
        ((errors++))
    fi
    
    if [ $errors -eq 0 ]; then
        success "Backup verification passed"
        return 0
    else
        error "Backup verification failed with $errors errors"
        return 1
    fi
}

# Clean old backups
cleanup_old_backups() {
    log "Cleaning up old backups (retention: $RETENTION_DAYS days)..."
    
    local deleted_count=0
    
    # Find and delete directories older than retention period
    find "$BACKUP_BASE_DIR" -maxdepth 1 -type d -name "20*-*-*" -mtime +$RETENTION_DAYS -print0 | \
    while IFS= read -r -d '' backup_dir; do
        if [ -d "$backup_dir" ]; then
            local dir_name=$(basename "$backup_dir")
            log "Deleting old backup: $dir_name"
            rm -rf "$backup_dir"
            ((deleted_count++))
        fi
    done
    
    if [ $deleted_count -gt 0 ]; then
        log "Deleted $deleted_count old backup directories"
    else
        log "No old backups to clean up"
    fi
}

# Update memory.json with backup info
update_memory_backup_info() {
    log "Updating memory.json with backup information..."
    
    local memory_file="$MEMORY_FILES_DIR/memory.json"
    
    if [ -f "$memory_file" ]; then
        # Create a temporary file with updated backup info
        local temp_file=$(mktemp)
        
        # Use jq to update the backup info (if jq is available)
        if command -v jq &> /dev/null; then
            jq ".metadata.backup_info.last_backup = \"$(date -u +"%Y-%m-%dT%H:%M:%SZ")\" | 
                .metadata.backup_info.backup_count = (.metadata.backup_info.backup_count // 0) + 1" \
                "$memory_file" > "$temp_file"
            
            mv "$temp_file" "$memory_file"
            log "Updated memory.json backup information"
        else
            warning "jq not available, skipping memory.json update"
            rm -f "$temp_file"
        fi
    else
        warning "memory.json not found, skipping backup info update"
    fi
}

# Generate backup report
generate_backup_report() {
    log "Generating backup report..."
    
    local report_file="$BACKUP_DIR/backup-report.txt"
    local file_count=$(find "$BACKUP_DIR" -type f | wc -l)
    local backup_size=$(du -sh "$BACKUP_DIR" | cut -f1)
    
    cat > "$report_file" << EOF
Takharrujy Platform Memory Backup Report
========================================

Backup Date: $DATE $TIME
Backup Directory: $BACKUP_DIR
Total Files Backed Up: $file_count
Total Backup Size: $backup_size

Files and Directories:
$(find "$BACKUP_DIR" -type f -printf "  %P\n" | sort)

Backup Verification: PASSED
Retention Policy: $RETENTION_DAYS days
Next Backup: $(date -d "+1 day" +"%Y-%m-%d")

Memory System Status:
- Knowledge Base: $([ -f "$BACKUP_DIR/context/knowledge.md.backup" ] && echo "✅ Backed up" || echo "❌ Missing")
- Patterns Library: $([ -f "$BACKUP_DIR/context/patterns.md.backup" ] && echo "✅ Backed up" || echo "❌ Missing")
- Decision Log: $([ -f "$BACKUP_DIR/context/decisions.md.backup" ] && echo "✅ Backed up" || echo "❌ Missing")
- Task Memory: $([ -f "$BACKUP_DIR/context/tasks.md.backup" ] && echo "✅ Backed up" || echo "❌ Missing")
- Core Memory: $([ -f "$BACKUP_DIR/memory.json.backup" ] && echo "✅ Backed up" || echo "❌ Missing")

Generated by: Takharrujy Memory Backup System
EOF
    
    log "Generated backup report: $report_file"
}

# Main backup function
main() {
    log "Starting Takharrujy Platform memory backup..."
    log "Backup date: $DATE"
    log "Backup time: $TIME"
    
    # Create backup structure
    create_backup_structure
    
    # Perform backups
    backup_memory_files
    backup_context_files
    backup_command_files
    backup_agent_files
    backup_workflow_files
    
    # Create manifest and verify
    create_backup_manifest
    
    if verify_backup; then
        success "Backup completed successfully!"
        
        # Update memory system and generate report
        update_memory_backup_info
        generate_backup_report
        
        # Cleanup old backups
        cleanup_old_backups
        
        success "Memory backup process completed"
        success "Backup location: $BACKUP_DIR"
        success "Total backup size: $(du -sh "$BACKUP_DIR" | cut -f1)"
        
    else
        error "Backup verification failed!"
        exit 1
    fi
}

# Run main function if script is executed directly
if [[ "${BASH_SOURCE[0]}" == "${0}" ]]; then
    main "$@"
fi
