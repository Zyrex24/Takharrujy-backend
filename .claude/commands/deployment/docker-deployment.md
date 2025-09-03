# Docker Deployment Commands

## Container Configuration
- **Base Image:** openjdk:24-jre-slim
- **Application Server:** Spring Boot embedded Tomcat
- **Database:** PostgreSQL 16.x container
- **Cache:** Redis 7.x container
- **Reverse Proxy:** Nginx
- **Orchestration:** Docker Compose

## Dockerfile Configuration

### 1. Multi-stage Dockerfile
```dockerfile
# Build stage
FROM maven:3.9-openjdk-24 AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:24-jre-slim
WORKDIR /app

# Install required packages
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r takharrujy && useradd -r -g takharrujy takharrujy

# Copy application jar
COPY --from=build /app/target/takharrujy-*.jar app.jar

# Set ownership
RUN chown -R takharrujy:takharrujy /app
USER takharrujy

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# Run application
ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+UnlockExperimentalVMOptions", \
           "-XX:+EnableJVMCI", "--enable-preview", \
           "-Djava.security.egd=file:/dev/./urandom", \
           "-jar", "app.jar"]
```

### 2. Docker Compose Configuration
```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    container_name: takharrujy-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/takharrujy_db
      - SPRING_DATASOURCE_USERNAME=takharrujy_user
      - SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
      - AZURE_STORAGE_CONNECTION_STRING=${AZURE_STORAGE_CONNECTION_STRING}
      - BREVO_SMTP_KEY=${BREVO_SMTP_KEY}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_started
    networks:
      - takharrujy-network
    volumes:
      - app-logs:/app/logs
    restart: unless-stopped

  postgres:
    image: postgres:16-alpine
    container_name: takharrujy-postgres
    environment:
      - POSTGRES_DB=takharrujy_db
      - POSTGRES_USER=takharrujy_user
      - POSTGRES_PASSWORD=${DB_PASSWORD}
      - POSTGRES_INITDB_ARGS=--encoding=UTF-8 --lc-collate=ar_SA.UTF-8 --lc-ctype=ar_SA.UTF-8
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./database/init:/docker-entrypoint-initdb.d
    networks:
      - takharrujy-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U takharrujy_user -d takharrujy_db"]
      interval: 30s
      timeout: 10s
      retries: 5
    restart: unless-stopped

  redis:
    image: redis:7-alpine
    container_name: takharrujy-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
      - ./redis/redis.conf:/usr/local/etc/redis/redis.conf
    command: redis-server /usr/local/etc/redis/redis.conf
    networks:
      - takharrujy-network
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 30s
      timeout: 10s
      retries: 5
    restart: unless-stopped

  nginx:
    image: nginx:alpine
    container_name: takharrujy-nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/ssl:/etc/nginx/ssl
      - nginx-logs:/var/log/nginx
    depends_on:
      - app
    networks:
      - takharrujy-network
    restart: unless-stopped

networks:
  takharrujy-network:
    driver: bridge

volumes:
  postgres-data:
  redis-data:
  app-logs:
  nginx-logs:
```

## Production Configuration

### 1. Environment-specific Compose
```yaml
# docker-compose.prod.yml
version: '3.8'

services:
  app:
    image: takharrujy/backend:${VERSION}
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - JAVA_OPTS=-Xms2g -Xmx4g -XX:+UseZGC
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 4G
          cpus: '2.0'
        reservations:
          memory: 2G
          cpus: '1.0'
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  postgres:
    environment:
      - POSTGRES_SHARED_PRELOAD_LIBRARIES=pg_stat_statements
    command: >
      postgres
      -c max_connections=200
      -c shared_buffers=256MB
      -c effective_cache_size=1GB
      -c maintenance_work_mem=64MB
      -c checkpoint_completion_target=0.9
      -c wal_buffers=16MB
      -c default_statistics_target=100
      -c random_page_cost=1.1
      -c effective_io_concurrency=200
    deploy:
      resources:
        limits:
          memory: 2G
          cpus: '1.0'

  redis:
    command: >
      redis-server
      --maxmemory 512mb
      --maxmemory-policy allkeys-lru
      --appendonly yes
      --appendfsync everysec
```

### 2. Nginx Configuration
```nginx
# nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream takharrujy_backend {
        server app:8080;
    }

    # Rate limiting
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=auth:10m rate=5r/s;

    # SSL Configuration
    ssl_certificate /etc/nginx/ssl/takharujy.tech.crt;
    ssl_certificate_key /etc/nginx/ssl/takharujy.tech.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512;
    ssl_prefer_server_ciphers off;

    server {
        listen 80;
        server_name api.takharujy.tech;
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name api.takharujy.tech;

        # Security headers
        add_header X-Frame-Options DENY;
        add_header X-Content-Type-Options nosniff;
        add_header X-XSS-Protection "1; mode=block";
        add_header Strict-Transport-Security "max-age=31536000; includeSubDomains";

        # API endpoints
        location /api/ {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://takharrujy_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_connect_timeout 30s;
            proxy_send_timeout 30s;
            proxy_read_timeout 30s;
        }

        # Authentication endpoints with stricter rate limiting
        location /api/v1/auth/ {
            limit_req zone=auth burst=10 nodelay;
            proxy_pass http://takharrujy_backend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # Health check
        location /actuator/health {
            proxy_pass http://takharrujy_backend;
            access_log off;
        }
    }
}
```

## Deployment Scripts

### 1. Build and Deploy Script
```bash
#!/bin/bash
# deploy.sh

set -e

VERSION=${1:-latest}
ENVIRONMENT=${2:-staging}

echo "Deploying Takharrujy Backend v$VERSION to $ENVIRONMENT"

# Build Docker image
echo "Building Docker image..."
docker build -t takharrujy/backend:$VERSION .

# Tag for registry
docker tag takharrujy/backend:$VERSION takharrujy/backend:latest

# Push to registry (if not local deployment)
if [ "$ENVIRONMENT" != "local" ]; then
    echo "Pushing to Docker registry..."
    docker push takharrujy/backend:$VERSION
    docker push takharrujy/backend:latest
fi

# Deploy based on environment
case $ENVIRONMENT in
    "production")
        echo "Deploying to production..."
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
        ;;
    "staging")
        echo "Deploying to staging..."
        docker-compose -f docker-compose.yml -f docker-compose.staging.yml up -d
        ;;
    "local")
        echo "Deploying locally..."
        docker-compose up -d
        ;;
    *)
        echo "Unknown environment: $ENVIRONMENT"
        exit 1
        ;;
esac

# Wait for services to be healthy
echo "Waiting for services to be healthy..."
./scripts/wait-for-health.sh

echo "Deployment completed successfully!"
```

### 2. Health Check Script
```bash
#!/bin/bash
# scripts/wait-for-health.sh

MAX_ATTEMPTS=30
ATTEMPT=1

check_service_health() {
    local service_name=$1
    local health_url=$2
    
    echo "Checking health of $service_name..."
    
    while [ $ATTEMPT -le $MAX_ATTEMPTS ]; do
        if curl -f -s $health_url > /dev/null; then
            echo "$service_name is healthy!"
            return 0
        fi
        
        echo "Attempt $ATTEMPT/$MAX_ATTEMPTS: $service_name not ready yet..."
        sleep 10
        ATTEMPT=$((ATTEMPT + 1))
    done
    
    echo "ERROR: $service_name failed to become healthy"
    return 1
}

# Check application health
check_service_health "Application" "http://localhost:8080/actuator/health"

# Check database connectivity through application
check_service_health "Database" "http://localhost:8080/actuator/health/db"

# Check Redis connectivity through application
check_service_health "Redis" "http://localhost:8080/actuator/health/redis"

echo "All services are healthy!"
```

## Monitoring and Logging

### 1. Application Monitoring
```yaml
# docker-compose.monitoring.yml
version: '3.8'

services:
  prometheus:
    image: prom/prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - takharrujy-network

  grafana:
    image: grafana/grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/var/lib/grafana/dashboards
    networks:
      - takharrujy-network

volumes:
  grafana-data:
```

### 2. Centralized Logging
```yaml
# ELK Stack for logging
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - takharrujy-network

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    volumes:
      - ./logging/logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    networks:
      - takharrujy-network

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    networks:
      - takharrujy-network
```

## Backup and Recovery

### 1. Database Backup Container
```yaml
  db-backup:
    image: postgres:16-alpine
    environment:
      - PGPASSWORD=${DB_PASSWORD}
    volumes:
      - ./backups:/backups
      - backup-scripts:/scripts
    command: |
      sh -c '
        while true; do
          pg_dump -h postgres -U takharrujy_user -d takharrujy_db > /backups/backup_$$(date +%Y%m%d_%H%M%S).sql
          find /backups -name "*.sql" -mtime +7 -delete
          sleep 86400
        done
      '
    networks:
      - takharrujy-network
    depends_on:
      - postgres
```

## Useful Docker Commands

```bash
# Build and start services
docker-compose up -d --build

# View logs
docker-compose logs -f app
docker-compose logs -f postgres
docker-compose logs -f redis

# Scale application
docker-compose up -d --scale app=3

# Execute commands in containers
docker-compose exec app bash
docker-compose exec postgres psql -U takharrujy_user -d takharrujy_db

# Monitor resource usage
docker stats

# Clean up unused resources
docker system prune -a

# Backup volumes
docker run --rm -v takharrujy_postgres-data:/data -v $(pwd)/backup:/backup alpine tar czf /backup/postgres-backup.tar.gz /data

# Restore volumes
docker run --rm -v takharrujy_postgres-data:/data -v $(pwd)/backup:/backup alpine tar xzf /backup/postgres-backup.tar.gz -C /
```
