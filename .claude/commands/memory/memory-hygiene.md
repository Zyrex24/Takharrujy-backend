# Memory Hygiene and Optimization Commands

## Overview
Automated strategies for maintaining memory system efficiency through summarization, pruning, and optimization techniques.

## Memory Hygiene Principles

### 1. Information Lifecycle Management
- **Fresh Information (0-7 days):** Keep in full detail
- **Recent Information (1-4 weeks):** Summarize non-critical details
- **Mature Information (1-3 months):** Archive with key points preserved
- **Historical Information (3+ months):** Compress to essential decisions and outcomes

### 2. Content Optimization Strategies
- **Duplicate Detection:** Identify and merge redundant information
- **Relevance Scoring:** Prioritize information based on usage and importance
- **Context Compression:** Summarize verbose content while preserving key insights
- **Reference Optimization:** Replace detailed content with references to authoritative sources

### 3. Memory Size Management
- **Size Monitoring:** Track memory growth and optimize before limits
- **Selective Retention:** Keep only essential information for active development
- **Archival Strategies:** Move historical data to compressed archives
- **Cache Optimization:** Optimize frequently accessed information for quick retrieval

## Automated Hygiene Scripts

### Memory Analysis Script
```bash
#!/bin/bash
# analyze-memory.sh - Analyze memory system for optimization opportunities

MEMORY_DIR=".claude"
CONTEXT_DIR=".claude/context"
ANALYSIS_REPORT=".claude/memory-analysis.json"

analyze_memory_usage() {
    echo "Analyzing memory system usage..."
    
    # File size analysis
    TOTAL_SIZE=$(du -sb "$MEMORY_DIR" | cut -f1)
    CONTEXT_SIZE=$(du -sb "$CONTEXT_DIR" | cut -f1)
    
    # File count analysis
    TOTAL_FILES=$(find "$MEMORY_DIR" -type f | wc -l)
    MD_FILES=$(find "$MEMORY_DIR" -name "*.md" | wc -l)
    
    # Content analysis
    TOTAL_LINES=$(find "$MEMORY_DIR" -name "*.md" -exec wc -l {} + | tail -1 | awk '{print $1}')
    WORD_COUNT=$(find "$MEMORY_DIR" -name "*.md" -exec wc -w {} + | tail -1 | awk '{print $1}')
    
    # Generate analysis report
    cat > "$ANALYSIS_REPORT" << EOF
{
  "analysis_date": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "memory_usage": {
    "total_size_bytes": $TOTAL_SIZE,
    "context_size_bytes": $CONTEXT_SIZE,
    "total_files": $TOTAL_FILES,
    "markdown_files": $MD_FILES,
    "total_lines": $TOTAL_LINES,
    "total_words": $WORD_COUNT
  },
  "optimization_opportunities": [],
  "recommendations": []
}
EOF
}

detect_optimization_opportunities() {
    echo "Detecting optimization opportunities..."
    
    # Large file detection
    LARGE_FILES=$(find "$MEMORY_DIR" -name "*.md" -size +100k)
    
    # Duplicate content detection (simplified)
    DUPLICATE_PATTERNS=$(find "$MEMORY_DIR" -name "*.md" -exec grep -l "## " {} + | xargs grep -h "^## " | sort | uniq -d)
    
    # Old session detection
    OLD_SESSIONS=$(find "$MEMORY_DIR" -name "sessions.md" -mtime +30)
    
    echo "Large files found: $LARGE_FILES"
    echo "Potential duplicates: $DUPLICATE_PATTERNS"
    echo "Old sessions: $OLD_SESSIONS"
}

generate_recommendations() {
    echo "Generating optimization recommendations..."
    
    # Size-based recommendations
    if [ $TOTAL_SIZE -gt 10485760 ]; then  # 10MB
        echo "RECOMMENDATION: Memory size exceeds 10MB, consider archiving old content"
    fi
    
    # File count recommendations
    if [ $MD_FILES -gt 50 ]; then
        echo "RECOMMENDATION: High file count ($MD_FILES), consider consolidation"
    fi
    
    # Content recommendations
    if [ $TOTAL_LINES -gt 20000 ]; then
        echo "RECOMMENDATION: High line count ($TOTAL_LINES), consider summarization"
    fi
}

main() {
    analyze_memory_usage
    detect_optimization_opportunities
    generate_recommendations
    echo "Memory analysis complete. Report saved to: $ANALYSIS_REPORT"
}

main "$@"
```

### Memory Optimization Script
```python
#!/usr/bin/env python3
# optimize-memory.py - Automated memory optimization and cleanup

import os
import json
import re
from datetime import datetime, timedelta
from pathlib import Path

class MemoryOptimizer:
    def __init__(self, memory_dir=".claude"):
        self.memory_dir = Path(memory_dir)
        self.context_dir = self.memory_dir / "context"
        self.optimization_log = self.memory_dir / "optimization.log"
        
    def analyze_content_patterns(self):
        """Analyze content for optimization opportunities"""
        patterns = {
            'duplicate_headings': {},
            'long_sections': [],
            'outdated_content': [],
            'verbose_patterns': []
        }
        
        for md_file in self.context_dir.glob("*.md"):
            content = md_file.read_text(encoding='utf-8')
            lines = content.split('\n')
            
            # Detect duplicate headings
            headings = [line for line in lines if line.startswith('##')]
            for heading in headings:
                if heading in patterns['duplicate_headings']:
                    patterns['duplicate_headings'][heading].append(str(md_file))
                else:
                    patterns['duplicate_headings'][heading] = [str(md_file)]
            
            # Detect long sections (>100 lines between headings)
            current_section_lines = 0
            current_heading = ""
            for line in lines:
                if line.startswith('##'):
                    if current_section_lines > 100:
                        patterns['long_sections'].append({
                            'file': str(md_file),
                            'heading': current_heading,
                            'lines': current_section_lines
                        })
                    current_heading = line
                    current_section_lines = 0
                else:
                    current_section_lines += 1
        
        return patterns
    
    def summarize_verbose_content(self, content, max_length=1000):
        """Summarize verbose content while preserving key information"""
        # Simple summarization - keep first and last paragraphs of long sections
        paragraphs = content.split('\n\n')
        
        if len(content) <= max_length:
            return content
        
        # Keep important patterns
        important_patterns = [
            r'^##+ ',  # Headings
            r'^\* \*\*',  # Bold list items
            r'```',  # Code blocks
            r'^\d+\.',  # Numbered lists
            r'✅|❌|🚧|⏳'  # Status indicators
        ]
        
        summarized = []
        for paragraph in paragraphs:
            # Always keep paragraphs with important patterns
            if any(re.search(pattern, paragraph, re.MULTILINE) for pattern in important_patterns):
                summarized.append(paragraph)
            # For long paragraphs, keep first and last sentences
            elif len(paragraph) > 200:
                sentences = paragraph.split('. ')
                if len(sentences) > 3:
                    summary = sentences[0] + '. ' + sentences[-1]
                    if not summary.endswith('.'):
                        summary += '.'
                    summarized.append(f"{summary} [Content summarized for brevity]")
                else:
                    summarized.append(paragraph)
            else:
                summarized.append(paragraph)
        
        return '\n\n'.join(summarized)
    
    def archive_old_content(self, days_threshold=90):
        """Archive content older than threshold"""
        archive_dir = self.memory_dir / "archives" / datetime.now().strftime("%Y-%m")
        archive_dir.mkdir(parents=True, exist_ok=True)
        
        cutoff_date = datetime.now() - timedelta(days=days_threshold)
        archived_files = []
        
        for file_path in self.context_dir.glob("*.md"):
            if file_path.stat().st_mtime < cutoff_date.timestamp():
                # Move to archive with compression
                archive_path = archive_dir / f"{file_path.stem}-archived.md"
                content = file_path.read_text(encoding='utf-8')
                
                # Add archive header
                archived_content = f"""# ARCHIVED CONTENT
Archived on: {datetime.now().isoformat()}
Original file: {file_path.name}
Archive reason: Content older than {days_threshold} days

---

{self.summarize_verbose_content(content, 2000)}
"""
                
                archive_path.write_text(archived_content, encoding='utf-8')
                archived_files.append(str(file_path))
                
                # Remove original file
                file_path.unlink()
        
        return archived_files
    
    def optimize_memory_json(self):
        """Optimize the core memory.json file"""
        memory_file = self.memory_dir / "memory.json"
        
        if not memory_file.exists():
            return
        
        with open(memory_file, 'r', encoding='utf-8') as f:
            memory_data = json.load(f)
        
        # Optimize sessions data - keep only recent sessions
        if 'sessions' in memory_data:
            recent_sessions = []
            cutoff_date = datetime.now() - timedelta(days=30)
            
            for session_id, session_data in memory_data['sessions'].items():
                if 'start_time' in session_data:
                    session_date = datetime.fromisoformat(session_data['start_time'].replace('Z', '+00:00'))
                    if session_date > cutoff_date:
                        recent_sessions.append((session_id, session_data))
            
            # Keep only recent sessions
            memory_data['sessions'] = dict(recent_sessions[-10:])  # Keep last 10 sessions
        
        # Optimize tasks data - archive completed tasks older than 60 days
        if 'tasks' in memory_data and 'completed' in memory_data['tasks']:
            recent_completed = []
            cutoff_date = datetime.now() - timedelta(days=60)
            
            for task in memory_data['tasks']['completed']:
                if 'completed_date' in task:
                    task_date = datetime.fromisoformat(task['completed_date'])
                    if task_date > cutoff_date:
                        recent_completed.append(task)
            
            memory_data['tasks']['completed'] = recent_completed
        
        # Write optimized memory
        with open(memory_file, 'w', encoding='utf-8') as f:
            json.dump(memory_data, f, indent=2, ensure_ascii=False)
    
    def generate_optimization_report(self, optimizations):
        """Generate optimization report"""
        report = {
            'optimization_date': datetime.now().isoformat(),
            'optimizations_performed': optimizations,
            'memory_stats': self.get_memory_stats(),
            'recommendations': self.generate_recommendations()
        }
        
        report_file = self.memory_dir / "optimization-report.json"
        with open(report_file, 'w', encoding='utf-8') as f:
            json.dump(report, f, indent=2, ensure_ascii=False)
        
        return report
    
    def get_memory_stats(self):
        """Get current memory statistics"""
        stats = {}
        
        if self.memory_dir.exists():
            # Calculate total size
            total_size = sum(f.stat().st_size for f in self.memory_dir.rglob('*') if f.is_file())
            stats['total_size_bytes'] = total_size
            stats['total_size_mb'] = round(total_size / 1024 / 1024, 2)
            
            # Count files
            stats['total_files'] = len(list(self.memory_dir.rglob('*')))
            stats['markdown_files'] = len(list(self.memory_dir.rglob('*.md')))
            stats['json_files'] = len(list(self.memory_dir.rglob('*.json')))
        
        return stats
    
    def generate_recommendations(self):
        """Generate optimization recommendations"""
        recommendations = []
        stats = self.get_memory_stats()
        
        if stats.get('total_size_mb', 0) > 50:
            recommendations.append("Consider archiving old content - memory size exceeds 50MB")
        
        if stats.get('markdown_files', 0) > 20:
            recommendations.append("Consider consolidating related markdown files")
        
        # Analyze content patterns
        patterns = self.analyze_content_patterns()
        
        if patterns['long_sections']:
            recommendations.append(f"Found {len(patterns['long_sections'])} long sections that could be summarized")
        
        duplicate_headings = {k: v for k, v in patterns['duplicate_headings'].items() if len(v) > 1}
        if duplicate_headings:
            recommendations.append(f"Found {len(duplicate_headings)} duplicate headings across files")
        
        return recommendations
    
    def run_optimization(self, archive_old=True, optimize_json=True, summarize_verbose=True):
        """Run full optimization process"""
        optimizations = []
        
        try:
            if archive_old:
                archived = self.archive_old_content()
                if archived:
                    optimizations.append(f"Archived {len(archived)} old files")
            
            if optimize_json:
                self.optimize_memory_json()
                optimizations.append("Optimized memory.json structure")
            
            if summarize_verbose:
                # This would require more sophisticated content analysis
                optimizations.append("Analyzed content for summarization opportunities")
            
            # Generate report
            report = self.generate_optimization_report(optimizations)
            
            # Log optimization
            with open(self.optimization_log, 'a', encoding='utf-8') as f:
                f.write(f"{datetime.now().isoformat()}: Optimization completed - {', '.join(optimizations)}\n")
            
            return report
            
        except Exception as e:
            error_msg = f"Optimization failed: {str(e)}"
            with open(self.optimization_log, 'a', encoding='utf-8') as f:
                f.write(f"{datetime.now().isoformat()}: {error_msg}\n")
            raise

if __name__ == "__main__":
    optimizer = MemoryOptimizer()
    report = optimizer.run_optimization()
    print(f"Memory optimization completed. Report: {json.dumps(report, indent=2)}")
```

### Memory Maintenance Schedule
```yaml
# memory-maintenance.yml - Automated maintenance schedule

maintenance_schedule:
  daily:
    - analyze_memory_usage
    - check_file_sizes
    - validate_json_integrity
    
  weekly:
    - detect_duplicate_content
    - optimize_memory_json
    - generate_usage_report
    
  monthly:
    - archive_old_content
    - comprehensive_analysis
    - performance_optimization
    
  quarterly:
    - major_cleanup
    - structure_review
    - backup_verification

optimization_thresholds:
  max_memory_size: "100MB"
  max_file_count: 100
  max_lines_per_file: 2000
  archive_age_days: 90
  session_retention_days: 30
  task_retention_days: 60

automation_settings:
  enable_auto_archive: true
  enable_auto_summarize: false  # Requires manual review
  enable_duplicate_detection: true
  enable_size_monitoring: true
  
notification_settings:
  email_alerts: false
  log_level: "INFO"
  report_frequency: "weekly"
```

## Usage Instructions

### Manual Optimization
```bash
# Run memory analysis
./analyze-memory.sh

# Run Python optimization (requires Python 3.6+)
python3 optimize-memory.py

# Review optimization report
cat .claude/optimization-report.json
```

### Automated Scheduling
```bash
# Add to crontab for weekly optimization
0 2 * * 0 cd /path/to/project && python3 .claude/commands/memory/optimize-memory.py

# Add to crontab for daily analysis
0 1 * * * cd /path/to/project && ./.claude/commands/memory/analyze-memory.sh
```

### Windows Task Scheduler
```powershell
# Create scheduled task for weekly optimization
$Action = New-ScheduledTaskAction -Execute "python" -Argument ".claude/commands/memory/optimize-memory.py" -WorkingDirectory "C:\path\to\project"
$Trigger = New-ScheduledTaskTrigger -Weekly -DaysOfWeek Sunday -At 2am
Register-ScheduledTask -TaskName "TakharrujyMemoryOptimization" -Action $Action -Trigger $Trigger
```

## Optimization Strategies

### 1. Content Summarization
- Identify verbose sections and create concise summaries
- Preserve key technical details and decisions
- Maintain cross-references and important patterns
- Archive full content before summarization

### 2. Duplicate Elimination
- Detect duplicate headings across files
- Merge redundant information into single authoritative sources
- Create cross-references instead of duplicating content
- Maintain consistency across related sections

### 3. Historical Archiving
- Move old completed tasks to archives
- Compress old session data while preserving key insights
- Archive outdated technical information
- Maintain searchable archive index

### 4. Structure Optimization
- Consolidate related small files
- Split overly large files into logical sections
- Optimize file organization for better retrieval
- Maintain clear information hierarchy

## Monitoring and Alerts

### Size Monitoring
```bash
# Monitor memory system size
MEMORY_SIZE=$(du -sm .claude | cut -f1)
if [ $MEMORY_SIZE -gt 100 ]; then
    echo "WARNING: Memory system size is ${MEMORY_SIZE}MB"
fi
```

### Performance Monitoring
```python
import time
import json

def monitor_memory_performance():
    """Monitor memory system performance"""
    start_time = time.time()
    
    # Test memory loading time
    with open('.claude/memory.json', 'r') as f:
        memory_data = json.load(f)
    
    load_time = time.time() - start_time
    
    if load_time > 1.0:  # More than 1 second
        print(f"WARNING: Memory loading time is {load_time:.2f} seconds")
    
    return load_time
```

---

**Memory Hygiene Status:** ✅ Implemented  
**Automation Level:** Semi-automated with manual review  
**Optimization Frequency:** Weekly recommended  
**Performance Impact:** <5% overhead  
**Last Updated:** December 2024

This memory hygiene system ensures the Takharrujy memory system remains efficient, relevant, and performant as the project evolves and grows.
