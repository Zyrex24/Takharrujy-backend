# Database Management Commands

## Database Configuration
- **Database:** PostgreSQL 16.x
- **Migration Tool:** Flyway
- **Connection Pool:** HikariCP
- **Security:** Row-Level Security (RLS)
- **Indexing:** Advanced indexing strategies
- **Backup:** Automated backup procedures

## Database Setup Commands

### 1. Initial Database Setup
```sql
-- Create database
CREATE DATABASE takharrujy_db;

-- Create application user
CREATE USER takharrujy_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE takharrujy_db TO takharrujy_user;

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";
```

### 2. Flyway Migration Commands
```bash
# Check migration status
mvn flyway:info

# Migrate to latest version
mvn flyway:migrate

# Validate migrations
mvn flyway:validate

# Clean database (development only)
mvn flyway:clean

# Repair migration history
mvn flyway:repair

# Baseline existing database
mvn flyway:baseline
```

## Schema Management

### 1. Create Migration Files
```sql
-- V1__Create_base_tables.sql
CREATE TABLE universities (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    name_ar VARCHAR(200) NOT NULL,
    domain VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    university_id BIGINT NOT NULL REFERENCES universities(id),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    name VARCHAR(200) NOT NULL,
    name_ar VARCHAR(200),
    role user_role NOT NULL,
    is_active BOOLEAN DEFAULT true,
    email_verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Enable Row Level Security
ALTER TABLE users ENABLE ROW LEVEL SECURITY;

-- Create RLS policies
CREATE POLICY users_university_isolation ON users
    FOR ALL TO takharrujy_user
    USING (university_id = current_setting('app.current_university_id')::bigint);
```

### 2. Index Management
```sql
-- V2__Create_indexes.sql

-- Performance indexes
CREATE INDEX CONCURRENTLY idx_users_university_id ON users(university_id);
CREATE INDEX CONCURRENTLY idx_users_email ON users(email);
CREATE INDEX CONCURRENTLY idx_users_role ON users(role);

-- Full-text search indexes for Arabic content
CREATE INDEX CONCURRENTLY idx_projects_title_search ON projects 
    USING gin(to_tsvector('arabic', title));
CREATE INDEX CONCURRENTLY idx_projects_description_search ON projects 
    USING gin(to_tsvector('arabic', description));

-- Composite indexes for common queries
CREATE INDEX CONCURRENTLY idx_projects_university_status ON projects(university_id, status);
CREATE INDEX CONCURRENTLY idx_tasks_project_assignee ON tasks(project_id, assigned_to);

-- Partial indexes for active records
CREATE INDEX CONCURRENTLY idx_active_projects ON projects(id) 
    WHERE status != 'ARCHIVED';
```

### 3. Data Constraints and Validation
```sql
-- V3__Add_constraints.sql

-- Check constraints for data validation
ALTER TABLE projects ADD CONSTRAINT chk_project_title_length 
    CHECK (char_length(title) >= 3 AND char_length(title) <= 200);

ALTER TABLE projects ADD CONSTRAINT chk_project_dates 
    CHECK (end_date > start_date);

-- Enum constraints
CREATE TYPE project_status AS ENUM (
    'DRAFT', 'ACTIVE', 'SUBMITTED', 'UNDER_REVIEW', 
    'APPROVED', 'REJECTED', 'COMPLETED', 'ARCHIVED'
);

CREATE TYPE task_priority AS ENUM ('LOW', 'MEDIUM', 'HIGH', 'URGENT');

CREATE TYPE file_type AS ENUM (
    'DOCUMENT', 'IMAGE', 'VIDEO', 'AUDIO', 'ARCHIVE', 'OTHER'
);
```

## Performance Optimization

### 1. Query Optimization
```sql
-- Analyze query performance
EXPLAIN (ANALYZE, BUFFERS, FORMAT JSON) 
SELECT p.*, u.name as supervisor_name
FROM projects p
JOIN users u ON p.supervisor_id = u.id
WHERE p.university_id = $1 AND p.status = 'ACTIVE';

-- Create materialized views for complex queries
CREATE MATERIALIZED VIEW project_statistics AS
SELECT 
    university_id,
    status,
    COUNT(*) as project_count,
    AVG(EXTRACT(DAY FROM (end_date - start_date))) as avg_duration_days
FROM projects
GROUP BY university_id, status;

-- Refresh materialized view
REFRESH MATERIALIZED VIEW CONCURRENTLY project_statistics;
```

### 2. Connection Pool Configuration
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      validation-timeout: 5000
      max-lifetime: 1200000
      leak-detection-threshold: 60000
```

## Database Monitoring

### 1. Performance Monitoring Queries
```sql
-- Check active connections
SELECT 
    datname,
    usename,
    application_name,
    client_addr,
    state,
    query_start,
    state_change,
    query
FROM pg_stat_activity
WHERE state != 'idle';

-- Monitor slow queries
SELECT 
    query,
    calls,
    total_time,
    mean_time,
    rows
FROM pg_stat_statements
ORDER BY total_time DESC
LIMIT 10;

-- Check index usage
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes
ORDER BY idx_scan DESC;
```

### 2. Database Health Checks
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM pg_stat_activity WHERE state = 'active'"
            );
            ResultSet resultSet = statement.executeQuery();
            
            int activeConnections = 0;
            while (resultSet.next()) {
                activeConnections++;
            }
            
            return Health.up()
                .withDetail("activeConnections", activeConnections)
                .withDetail("maxConnections", getMaxConnections(connection))
                .build();
                
        } catch (SQLException e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

## Backup and Recovery

### 1. Automated Backup Script
```bash
#!/bin/bash
# backup-database.sh

DB_NAME="takharrujy_db"
DB_USER="takharrujy_user"
BACKUP_DIR="/var/backups/postgresql"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${DATE}.sql"

# Create backup directory if it doesn't exist
mkdir -p $BACKUP_DIR

# Create database backup
pg_dump -h localhost -U $DB_USER -d $DB_NAME > $BACKUP_FILE

# Compress backup
gzip $BACKUP_FILE

# Remove backups older than 30 days
find $BACKUP_DIR -name "*.sql.gz" -mtime +30 -delete

echo "Backup completed: ${BACKUP_FILE}.gz"
```

### 2. Point-in-Time Recovery
```bash
# Enable WAL archiving in postgresql.conf
wal_level = replica
archive_mode = on
archive_command = 'cp %p /var/lib/postgresql/wal_archive/%f'

# Create base backup
pg_basebackup -h localhost -U takharrujy_user -D /var/lib/postgresql/backup -Ft -z -P

# Recovery configuration
# recovery.conf
restore_command = 'cp /var/lib/postgresql/wal_archive/%f %p'
recovery_target_time = '2024-12-20 10:00:00'
```

## Data Migration and Seeding

### 1. Data Seeding
```sql
-- V4__Seed_data.sql

-- Insert universities
INSERT INTO universities (name, name_ar, domain) VALUES
('King Saud University', 'جامعة الملك سعود', 'ksu.edu.sa'),
('King Abdulaziz University', 'جامعة الملك عبدالعزيز', 'kau.edu.sa'),
('King Fahd University', 'جامعة الملك فهد', 'kfupm.edu.sa');

-- Insert admin users
INSERT INTO users (university_id, email, password_hash, name, name_ar, role) VALUES
(1, 'admin@ksu.edu.sa', '$2a$10$...', 'Admin User', 'مدير النظام', 'ADMIN');
```

### 2. Data Migration Tools
```java
@Component
public class DataMigrationService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @EventListener(ApplicationReadyEvent.class)
    public void migrateExistingData() {
        // Migrate data from legacy system
        String sql = """
            INSERT INTO projects (title, description, university_id, created_at)
            SELECT title, description, university_id, created_date
            FROM legacy_projects
            WHERE migrated = false
            """;
        
        int rowsAffected = jdbcTemplate.update(sql);
        log.info("Migrated {} projects from legacy system", rowsAffected);
    }
}
```

## Database Testing

### 1. Integration Tests with Testcontainers
```java
@Testcontainers
@SpringBootTest
class DatabaseIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("takharrujy_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("test-schema.sql");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void shouldConnectToDatabase() {
        assertTrue(postgres.isRunning());
    }
}
```

## Useful Database Commands

```bash
# Connect to database
psql -h localhost -U takharrujy_user -d takharrujy_db

# Check database size
SELECT pg_size_pretty(pg_database_size('takharrujy_db'));

# Analyze database performance
ANALYZE;

# Vacuum database
VACUUM ANALYZE;

# Check table sizes
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;
```
