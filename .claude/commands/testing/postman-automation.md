# Postman Testing Automation Commands

## Overview
Comprehensive Postman testing strategy for Takharrujy Platform API endpoints. Each endpoint requires dedicated folder structure with environment files and collection tests.

## Postman Folder Structure Requirements

### Mandatory Structure (per endpoint)
```
postman/
├── auth-register/
│   ├── Takharrujy-Dev.postman_environment.json
│   ├── Takharrujy-Staging.postman_environment.json
│   ├── Takharrujy-Prod.postman_environment.json
│   └── Auth-Register.postman_collection.json
├── auth-login/
│   ├── Takharrujy-Dev.postman_environment.json
│   ├── Takharrujy-Staging.postman_environment.json
│   ├── Takharrujy-Prod.postman_environment.json
│   └── Auth-Login.postman_collection.json
├── projects-create/
│   ├── Takharrujy-Dev.postman_environment.json
│   ├── Takharrujy-Staging.postman_environment.json
│   ├── Takharrujy-Prod.postman_environment.json
│   └── Projects-Create.postman_collection.json
... (87 total endpoint folders)
```

## Environment File Templates

### Development Environment
```json
{
  "id": "dev-environment-id",
  "name": "Takharrujy-Dev",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080/api/v1",
      "type": "default",
      "enabled": true
    },
    {
      "key": "authToken",
      "value": "",
      "type": "secret",
      "enabled": true
    },
    {
      "key": "userId",
      "value": "",
      "type": "default",
      "enabled": true
    },
    {
      "key": "projectId",
      "value": "",
      "type": "default",
      "enabled": true
    },
    {
      "key": "universityId",
      "value": "1",
      "type": "default",
      "enabled": true
    },
    {
      "key": "supervisorId",
      "value": "",
      "type": "default",
      "enabled": true
    },
    {
      "key": "taskId",
      "value": "",
      "type": "default",
      "enabled": true
    },
    {
      "key": "fileId",
      "value": "",
      "type": "default",
      "enabled": true
    },
    {
      "key": "testEmail",
      "value": "test@cu.edu.eg",
      "type": "default",
      "enabled": true
    },
    {
      "key": "testPassword",
      "value": "TestPass123!",
      "type": "secret",
      "enabled": true
    }
  ]
}
```

### Staging Environment
```json
{
  "id": "staging-environment-id",
  "name": "Takharrujy-Staging",
  "values": [
    {
      "key": "baseUrl",
      "value": "https://staging-api.takharujy.tech/v1",
      "type": "default",
      "enabled": true
    },
    {
      "key": "authToken",
      "value": "",
      "type": "secret",
      "enabled": true
    }
  ]
}
```

### Production Environment
```json
{
  "id": "prod-environment-id",
  "name": "Takharrujy-Prod",
  "values": [
    {
      "key": "baseUrl",
      "value": "https://api.takharujy.tech/v1",
      "type": "default",
      "enabled": true
    },
    {
      "key": "authToken",
      "value": "",
      "type": "secret",
      "enabled": true
    }
  ]
}
```

## Collection Template Examples

### Authentication Register Collection
```json
{
  "info": {
    "name": "Auth-Register",
    "description": "User registration endpoint testing",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Happy Path - Valid Registration",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 201', function () {",
              "    pm.response.to.have.status(201);",
              "});",
              "",
              "pm.test('Response has success flag', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.success).to.be.true;",
              "});",
              "",
              "pm.test('User data is returned', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.data).to.have.property('id');",
              "    pm.expect(jsonData.data).to.have.property('email');",
              "    pm.expect(jsonData.data.email).to.eql(pm.environment.get('testEmail'));",
              "});",
              "",
              "pm.test('Arabic name handling', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.data.firstName).to.eql('أحمد');",
              "    pm.expect(jsonData.data.lastName).to.eql('محمد');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"{{testEmail}}\",\n  \"password\": \"{{testPassword}}\",\n  \"firstName\": \"أحمد\",\n  \"lastName\": \"محمد\",\n  \"role\": \"STUDENT\",\n  \"universityDomain\": \"cu.edu.eg\",\n  \"studentId\": \"20210001\",\n  \"department\": \"Computer Science\",\n  \"phoneNumber\": \"+201234567890\",\n  \"preferredLanguage\": \"ar\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "register"]
        }
      }
    },
    {
      "name": "Error - Invalid Email Format",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 400', function () {",
              "    pm.response.to.have.status(400);",
              "});",
              "",
              "pm.test('Error response structure', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.success).to.be.false;",
              "    pm.expect(jsonData.error).to.have.property('code');",
              "    pm.expect(jsonData.error.code).to.eql('VALIDATION_ERROR');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"invalid-email\",\n  \"password\": \"{{testPassword}}\",\n  \"firstName\": \"أحمد\",\n  \"lastName\": \"محمد\",\n  \"role\": \"STUDENT\",\n  \"universityDomain\": \"cu.edu.eg\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "register"]
        }
      }
    },
    {
      "name": "Error - Weak Password",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 400', function () {",
              "    pm.response.to.have.status(400);",
              "});",
              "",
              "pm.test('Password validation error', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.error.details.field).to.eql('password');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"{{testEmail}}\",\n  \"password\": \"123\",\n  \"firstName\": \"أحمد\",\n  \"lastName\": \"محمد\",\n  \"role\": \"STUDENT\",\n  \"universityDomain\": \"cu.edu.eg\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "register"]
        }
      }
    },
    {
      "name": "Edge Case - Arabic Characters Only",
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test('Status code is 201', function () {",
              "    pm.response.to.have.status(201);",
              "});",
              "",
              "pm.test('Arabic text preserved', function () {",
              "    const jsonData = pm.response.json();",
              "    pm.expect(jsonData.data.firstName).to.include('أ');",
              "    pm.expect(jsonData.data.department).to.include('علوم');",
              "});"
            ]
          }
        }
      ],
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"arabic-test@cu.edu.eg\",\n  \"password\": \"{{testPassword}}\",\n  \"firstName\": \"أحمد محمد علي\",\n  \"lastName\": \"عبدالرحمن الأحمدي\",\n  \"role\": \"STUDENT\",\n  \"universityDomain\": \"cu.edu.eg\",\n  \"department\": \"علوم الحاسوب والذكاء الاصطناعي\",\n  \"preferredLanguage\": \"ar\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "register"]
        }
      }
    }
  ]
}
```

## Pre-request Scripts

### Authentication Token Setup
```javascript
// Pre-request script for authenticated endpoints
if (!pm.environment.get("authToken")) {
    pm.sendRequest({
        url: pm.environment.get("baseUrl") + "/auth/login",
        method: 'POST',
        header: {
            'Content-Type': 'application/json',
        },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                email: pm.environment.get("testEmail"),
                password: pm.environment.get("testPassword")
            })
        }
    }, function (err, response) {
        if (err) {
            console.log("Authentication failed:", err);
        } else {
            const jsonData = response.json();
            if (jsonData.success && jsonData.data.token) {
                pm.environment.set("authToken", jsonData.data.token);
                pm.environment.set("userId", jsonData.data.user.id);
            }
        }
    });
}
```

### Arabic Text Validation
```javascript
// Pre-request script for Arabic text validation
pm.test("Arabic text encoding", function () {
    const arabicText = "مرحبا بك في منصة تخرجي";
    const encoded = encodeURIComponent(arabicText);
    const decoded = decodeURIComponent(encoded);
    pm.expect(decoded).to.eql(arabicText);
});
```

## Test Scripts

### Standard Response Validation
```javascript
// Standard test script for all endpoints
pm.test("Response time is acceptable", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});

pm.test("Content-Type is JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});

pm.test("Response has required structure", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData).to.have.property("success");
    pm.expect(jsonData).to.have.property("timestamp");
    
    if (jsonData.success) {
        pm.expect(jsonData).to.have.property("data");
        pm.expect(jsonData).to.have.property("message");
    } else {
        pm.expect(jsonData).to.have.property("error");
        pm.expect(jsonData.error).to.have.property("code");
        pm.expect(jsonData.error).to.have.property("message");
    }
});

pm.test("CORS headers present", function () {
    pm.expect(pm.response.headers.get("Access-Control-Allow-Origin")).to.not.be.null;
});
```

### Security Headers Validation
```javascript
// Security headers validation
pm.test("Security headers present", function () {
    pm.expect(pm.response.headers.get("X-Content-Type-Options")).to.eql("nosniff");
    pm.expect(pm.response.headers.get("X-Frame-Options")).to.eql("DENY");
    pm.expect(pm.response.headers.get("X-XSS-Protection")).to.eql("1; mode=block");
});

pm.test("No sensitive data in response headers", function () {
    const headers = pm.response.headers;
    pm.expect(headers.get("Server")).to.not.include("Apache");
    pm.expect(headers.get("Server")).to.not.include("nginx");
});
```

## CLI Commands for Automation

### Newman Collection Runner
```bash
# Install Newman globally
npm install -g newman

# Run single collection
newman run postman/auth-register/Auth-Register.postman_collection.json \
    -e postman/auth-register/Takharrujy-Dev.postman_environment.json \
    --reporters cli,json \
    --reporter-json-export results.json

# Run all collections in directory
for dir in postman/*/; do
    collection_file=$(find "$dir" -name "*.postman_collection.json")
    env_file=$(find "$dir" -name "*Dev.postman_environment.json")
    
    if [[ -f "$collection_file" && -f "$env_file" ]]; then
        echo "Running tests for $(basename "$dir")"
        newman run "$collection_file" -e "$env_file" --reporters cli
    fi
done
```

### Automated Test Execution
```bash
# Daily automated testing script
#!/bin/bash
ENVIRONMENTS=("Dev" "Staging")
RESULTS_DIR="test-results/$(date +%Y%m%d)"
mkdir -p "$RESULTS_DIR"

for env in "${ENVIRONMENTS[@]}"; do
    echo "Running tests against $env environment"
    
    for dir in postman/*/; do
        endpoint_name=$(basename "$dir")
        collection_file="$dir/$endpoint_name.postman_collection.json"
        env_file="$dir/Takharrujy-$env.postman_environment.json"
        
        if [[ -f "$collection_file" && -f "$env_file" ]]; then
            newman run "$collection_file" \
                -e "$env_file" \
                --reporters json \
                --reporter-json-export "$RESULTS_DIR/${endpoint_name}-${env}.json"
        fi
    done
done

# Generate summary report
node generate-test-report.js "$RESULTS_DIR"
```

## Developer Responsibilities

### Developer 1 - Authentication & Project Endpoints
**Required Postman Collections:**
1. `auth-register/` - User registration
2. `auth-login/` - User authentication  
3. `auth-refresh/` - Token refresh
4. `auth-logout/` - User logout
5. `auth-forgot-password/` - Password reset request
6. `auth-reset-password/` - Password reset confirmation
7. `projects-create/` - Project creation
8. `projects-update/` - Project updates
9. `projects-members-invite/` - Team invitations
10. `files-upload/` - File upload
11. `files-download/` - File download
12. `files-versions/` - File versioning
13. `messages-send/` - Real-time messaging

### Developer 2 - Task & Admin Endpoints  
**Required Postman Collections:**
1. `tasks-create/` - Task creation
2. `tasks-update/` - Task updates
3. `tasks-assign/` - Task assignment
4. `tasks-dependencies/` - Task dependencies
5. `notifications-send/` - Notifications
6. `notifications-preferences/` - Notification settings
7. `admin-users/` - User management
8. `admin-universities/` - University management
9. `supervisor-dashboard/` - Supervisor features
10. `deliverables-review/` - Deliverable reviews
11. `analytics-reports/` - System analytics

## Quality Assurance Checklist

### Collection Validation
- [ ] All HTTP methods tested (GET, POST, PUT, DELETE)
- [ ] Authentication scenarios covered
- [ ] Authorization edge cases tested
- [ ] Input validation comprehensive
- [ ] Arabic language support verified
- [ ] Error responses properly structured
- [ ] Performance benchmarks included
- [ ] Security headers validated

### Environment Configuration
- [ ] All required variables defined
- [ ] Sensitive data marked as secret
- [ ] Environment-specific URLs correct
- [ ] Test data realistic and diverse
- [ ] Arabic test data included

### Documentation Standards
- [ ] Request/response examples provided
- [ ] Arabic language examples included
- [ ] Error scenarios documented
- [ ] Pre-request scripts explained
- [ ] Test assertions documented

---

**Postman Testing Status:** ✅ Required for ALL 87 endpoints  
**Total Collections:** 87 collections + 261 environment files  
**Automation:** Newman CLI + CI/CD integration  
**Arabic Support:** ✅ Mandatory in all collections  
**Last Updated:** December 2024

This comprehensive Postman testing strategy ensures every endpoint is thoroughly tested with proper Arabic language support and automated validation across all environments.
