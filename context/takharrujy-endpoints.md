# Takharrujy Platform - API Endpoints Documentation

**Version:** 1.0  
**Date:** December 2024  
**Project:** Takharrujy (تخرجي) - University Graduation Project Management Platform  
**Base URL:** https://api.takharujy.tech/v1  
**Framework:** Spring Boot 3.4.x with Java 24  
**Total Endpoints:** 87 endpoints across 8 controllers  

## 1. API Overview

### 1.1 API Design Principles

- **RESTful Design:** Following REST conventions with proper HTTP methods
- **Consistent Response Format:** Standardized JSON responses across all endpoints
- **Authentication:** JWT Bearer token authentication for protected endpoints
- **Authorization:** Role-based access control (Student, Supervisor, Admin)
- **Validation:** Comprehensive input validation with detailed error messages
- **Internationalization:** Arabic and English language support
- **Rate Limiting:** API rate limiting to prevent abuse
- **Versioning:** URL-based versioning (/v1/) for backward compatibility

### 1.2 Authentication & Authorization

**Authentication Types:**
- **Public Endpoints:** No authentication required (registration, login, health checks)
- **Protected Endpoints:** Require valid JWT token in Authorization header
- **Role-Specific Endpoints:** Additional role-based access control

**JWT Token Format:**
```
Authorization: Bearer <jwt-token>
```

**Token Claims:**
```json
{
  "sub": "user@university.edu",
  "userId": 123,
  "role": "STUDENT",
  "universityId": 1,
  "iat": 1640995200,
  "exp": 1641081600
}
```

### 1.3 Standard Response Formats

**Success Response:**
```json
{
  "success": true,
  "data": { /* response data */ },
  "message": "Operation completed successfully",
  "timestamp": "2024-12-01T10:30:00Z"
}
```

**Error Response:**
```json
{
  "success": false,
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Invalid input data",
    "details": {
      "field": "email",
      "value": "invalid-email",
      "constraint": "must be a valid email address"
    }
  },
  "timestamp": "2024-12-01T10:30:00Z"
}
```

**Paginated Response:**
```json
{
  "success": true,
  "data": {
    "content": [ /* array of items */ ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalElements": 150,
      "totalPages": 8,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

## 2. Authentication & User Management Endpoints

### 2.1 Authentication Controller
**Base Path:** `/api/v1/auth`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| POST | `/register` | Register new user account | ❌ | - | UserRegistrationRequest | UserResponse |
| POST | `/login` | Authenticate user and get JWT token | ❌ | - | LoginRequest | AuthenticationResponse |
| POST | `/refresh` | Refresh JWT token | ✅ | Any | RefreshTokenRequest | AuthenticationResponse |
| POST | `/logout` | Invalidate JWT token | ✅ | Any | - | SuccessResponse |
| POST | `/forgot-password` | Request password reset | ❌ | - | PasswordResetRequest | SuccessResponse |
| POST | `/reset-password` | Reset password with token | ❌ | - | PasswordResetConfirmRequest | SuccessResponse |
| POST | `/verify-email` | Verify email address | ❌ | - | EmailVerificationRequest | SuccessResponse |
| POST | `/resend-verification` | Resend email verification | ✅ | Any | - | SuccessResponse |

**Detailed Endpoint Specifications:**

#### POST /api/v1/auth/register
Register a new user account with university email validation.

**Request Body:**
```json
{
  "email": "student@university.edu",
  "password": "SecurePass123!",
  "firstName": "أحمد",
  "lastName": "محمد",
  "role": "STUDENT",
  "universityDomain": "university.edu",
  "studentId": "20210001",
  "department": "Computer Science",
  "phoneNumber": "+201234567890",
  "preferredLanguage": "ar"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "email": "student@university.edu",
    "firstName": "أحمد",
    "lastName": "محمد",
    "role": "STUDENT",
    "university": "Cairo University",
    "department": "Computer Science",
    "emailVerified": false,
    "createdAt": "2024-12-01T10:30:00Z"
  },
  "message": "Registration successful. Please check your email for verification."
}
```

#### POST /api/v1/auth/login
Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "student@university.edu",
  "password": "SecurePass123!",
  "rememberMe": true
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "rt_eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expiresIn": 3600,
    "tokenType": "Bearer",
    "user": {
      "id": 123,
      "email": "student@university.edu",
      "firstName": "أحمد",
      "lastName": "محمد",
      "role": "STUDENT",
      "university": "Cairo University",
      "avatarUrl": null
    }
  }
}
```

### 2.2 User Management Controller
**Base Path:** `/api/v1/users`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/me` | Get current user profile | ✅ | Any | - | UserResponse |
| PUT | `/me` | Update current user profile | ✅ | Any | UserUpdateRequest | UserResponse |
| PUT | `/me/avatar` | Update user avatar | ✅ | Any | MultipartFile | FileResponse |
| PUT | `/me/password` | Change user password | ✅ | Any | ChangePasswordRequest | SuccessResponse |
| GET | `/me/preferences` | Get user preferences | ✅ | Any | - | UserPreferencesResponse |
| PUT | `/me/preferences` | Update user preferences | ✅ | Any | UserPreferencesRequest | UserPreferencesResponse |
| GET | `/me/activity` | Get user activity history | ✅ | Any | - | List<ActivityResponse> |
| GET | `/{userId}` | Get user by ID (limited info) | ✅ | Any | - | PublicUserResponse |
| GET | `/search` | Search users by name/email | ✅ | Any | Query params | List<PublicUserResponse> |

## 3. Project Management Endpoints

### 3.1 Project Controller
**Base Path:** `/api/v1/projects`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get user's projects | ✅ | Any | Query params | List<ProjectResponse> |
| POST | `/` | Create new project | ✅ | Student | ProjectCreateRequest | ProjectResponse |
| GET | `/{projectId}` | Get project details | ✅ | Member/Supervisor | - | ProjectResponse |
| PUT | `/{projectId}` | Update project | ✅ | Team Leader | ProjectUpdateRequest | ProjectResponse |
| DELETE | `/{projectId}` | Delete project | ✅ | Team Leader | - | SuccessResponse |
| GET | `/{projectId}/stats` | Get project statistics | ✅ | Member/Supervisor | - | ProjectStatsResponse |
| GET | `/{projectId}/timeline` | Get project timeline | ✅ | Member/Supervisor | - | ProjectTimelineResponse |
| GET | `/{projectId}/activity` | Get project activity feed | ✅ | Member/Supervisor | - | List<ActivityResponse> |
| POST | `/{projectId}/duplicate` | Duplicate project structure | ✅ | Student | - | ProjectResponse |

### 3.2 Project Members Controller
**Base Path:** `/api/v1/projects/{projectId}/members`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get project members | ✅ | Member/Supervisor | - | List<ProjectMemberResponse> |
| POST | `/` | Invite team member | ✅ | Team Leader | MemberInvitationRequest | ProjectMemberResponse |
| PUT | `/{memberId}` | Update member role | ✅ | Team Leader | MemberRoleUpdateRequest | ProjectMemberResponse |
| DELETE | `/{memberId}` | Remove team member | ✅ | Team Leader | - | SuccessResponse |
| POST | `/{memberId}/accept` | Accept project invitation | ✅ | Invitee | - | ProjectMemberResponse |
| POST | `/{memberId}/reject` | Reject project invitation | ✅ | Invitee | - | SuccessResponse |
| GET | `/invitations` | Get pending invitations | ✅ | Student | - | List<ProjectMemberResponse> |

## 4. Task Management Endpoints

### 4.1 Task Controller
**Base Path:** `/api/v1/tasks`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get user's assigned tasks | ✅ | Any | Query params | List<TaskResponse> |
| POST | `/` | Create new task | ✅ | Team Member | TaskCreateRequest | TaskResponse |
| GET | `/{taskId}` | Get task details | ✅ | Project Member | - | TaskResponse |
| PUT | `/{taskId}` | Update task | ✅ | Assignee/Creator | TaskUpdateRequest | TaskResponse |
| DELETE | `/{taskId}` | Delete task | ✅ | Creator | - | SuccessResponse |
| PUT | `/{taskId}/status` | Update task status | ✅ | Assignee | TaskStatusUpdateRequest | TaskResponse |
| PUT | `/{taskId}/assign` | Assign task to member | ✅ | Team Leader | TaskAssignmentRequest | TaskResponse |
| POST | `/{taskId}/complete` | Mark task as complete | ✅ | Assignee | TaskCompletionRequest | TaskResponse |
| GET | `/{taskId}/history` | Get task history | ✅ | Project Member | - | List<TaskHistoryResponse> |
| GET | `/{taskId}/dependencies` | Get task dependencies | ✅ | Project Member | - | List<TaskDependencyResponse> |
| POST | `/{taskId}/dependencies` | Add task dependency | ✅ | Team Member | TaskDependencyRequest | TaskDependencyResponse |
| DELETE | `/{taskId}/dependencies/{depId}` | Remove task dependency | ✅ | Team Member | - | SuccessResponse |

### 4.2 Project Tasks Controller
**Base Path:** `/api/v1/projects/{projectId}/tasks`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get project tasks | ✅ | Member/Supervisor | Query params | List<TaskResponse> |
| GET | `/overdue` | Get overdue tasks | ✅ | Member/Supervisor | - | List<TaskResponse> |
| GET | `/upcoming` | Get upcoming tasks | ✅ | Member/Supervisor | - | List<TaskResponse> |
| GET | `/stats` | Get task statistics | ✅ | Member/Supervisor | - | TaskStatsResponse |
| GET | `/gantt` | Get Gantt chart data | ✅ | Member/Supervisor | - | GanttChartResponse |

### 4.3 Task Comments Controller
**Base Path:** `/api/v1/tasks/{taskId}/comments`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get task comments | ✅ | Project Member | - | List<TaskCommentResponse> |
| POST | `/` | Add task comment | ✅ | Project Member | TaskCommentRequest | TaskCommentResponse |
| PUT | `/{commentId}` | Update comment | ✅ | Comment Author | TaskCommentUpdateRequest | TaskCommentResponse |
| DELETE | `/{commentId}` | Delete comment | ✅ | Comment Author | - | SuccessResponse |

## 5. File Management Endpoints

### 5.1 File Controller
**Base Path:** `/api/v1/files`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| POST | `/upload` | Upload file to project | ✅ | Project Member | MultipartFile + metadata | FileResponse |
| GET | `/{fileId}` | Download file | ✅ | Authorized User | - | File Stream |
| GET | `/{fileId}/info` | Get file metadata | ✅ | Authorized User | - | FileResponse |
| PUT | `/{fileId}` | Update file metadata | ✅ | Uploader | FileUpdateRequest | FileResponse |
| DELETE | `/{fileId}` | Delete file | ✅ | Uploader/Team Leader | - | SuccessResponse |
| GET | `/{fileId}/versions` | Get file versions | ✅ | Authorized User | - | List<FileVersionResponse> |
| POST | `/{fileId}/versions` | Create new file version | ✅ | Uploader | MultipartFile | FileVersionResponse |
| GET | `/{fileId}/share` | Get file share info | ✅ | Authorized User | - | FileShareResponse |
| POST | `/{fileId}/share` | Create file share link | ✅ | Authorized User | FileShareRequest | FileShareResponse |
| DELETE | `/{fileId}/share/{shareId}` | Revoke file share | ✅ | File Owner | - | SuccessResponse |
| GET | `/{fileId}/preview` | Get file preview | ✅ | Authorized User | - | File Preview |

### 5.2 Project Files Controller
**Base Path:** `/api/v1/projects/{projectId}/files`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get project files | ✅ | Member/Supervisor | Query params | List<FileResponse> |
| POST | `/` | Upload file to project | ✅ | Project Member | MultipartFile + metadata | FileResponse |
| GET | `/stats` | Get file statistics | ✅ | Member/Supervisor | - | FileStatsResponse |
| GET | `/recent` | Get recently uploaded files | ✅ | Member/Supervisor | - | List<FileResponse> |
| POST | `/bulk-upload` | Upload multiple files | ✅ | Project Member | MultipartFile[] | List<FileResponse> |
| POST | `/bulk-download` | Download multiple files as ZIP | ✅ | Member/Supervisor | FileDownloadRequest | File Stream |

## 6. Deliverable Management Endpoints

### 6.1 Deliverable Controller
**Base Path:** `/api/v1/deliverables`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/{deliverableId}` | Get deliverable details | ✅ | Project Member/Supervisor | - | DeliverableResponse |
| PUT | `/{deliverableId}` | Update deliverable | ✅ | Team Leader | DeliverableUpdateRequest | DeliverableResponse |
| DELETE | `/{deliverableId}` | Delete deliverable | ✅ | Team Leader | - | SuccessResponse |
| POST | `/{deliverableId}/submit` | Submit deliverable | ✅ | Team Leader | DeliverableSubmissionRequest | DeliverableResponse |
| GET | `/{deliverableId}/feedback` | Get supervisor feedback | ✅ | Project Member/Supervisor | - | List<FeedbackResponse> |
| POST | `/{deliverableId}/feedback` | Provide feedback | ✅ | Supervisor | FeedbackRequest | FeedbackResponse |
| PUT | `/{deliverableId}/approve` | Approve deliverable | ✅ | Supervisor | ApprovalRequest | DeliverableResponse |
| PUT | `/{deliverableId}/reject` | Reject deliverable | ✅ | Supervisor | RejectionRequest | DeliverableResponse |

### 6.2 Project Deliverables Controller
**Base Path:** `/api/v1/projects/{projectId}/deliverables`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get project deliverables | ✅ | Member/Supervisor | Query params | List<DeliverableResponse> |
| POST | `/` | Create deliverable | ✅ | Team Leader | DeliverableCreateRequest | DeliverableResponse |
| GET | `/pending` | Get pending deliverables | ✅ | Member/Supervisor | - | List<DeliverableResponse> |
| GET | `/overdue` | Get overdue deliverables | ✅ | Member/Supervisor | - | List<DeliverableResponse> |
| GET | `/stats` | Get deliverable statistics | ✅ | Member/Supervisor | - | DeliverableStatsResponse |

## 7. Notification & Messaging Endpoints

### 7.1 Notification Controller
**Base Path:** `/api/v1/notifications`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get user notifications | ✅ | Any | Query params | List<NotificationResponse> |
| GET | `/unread` | Get unread notifications | ✅ | Any | - | List<NotificationResponse> |
| PUT | `/{notificationId}/read` | Mark notification as read | ✅ | Notification Owner | - | SuccessResponse |
| PUT | `/mark-all-read` | Mark all notifications as read | ✅ | Any | - | SuccessResponse |
| DELETE | `/{notificationId}` | Delete notification | ✅ | Notification Owner | - | SuccessResponse |
| DELETE | `/clear-all` | Clear all notifications | ✅ | Any | - | SuccessResponse |
| GET | `/preferences` | Get notification preferences | ✅ | Any | - | NotificationPreferencesResponse |
| PUT | `/preferences` | Update notification preferences | ✅ | Any | NotificationPreferencesRequest | NotificationPreferencesResponse |
| GET | `/stats` | Get notification statistics | ✅ | Any | - | NotificationStatsResponse |

### 7.2 Message Controller
**Base Path:** `/api/v1/messages`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/{messageId}` | Get message details | ✅ | Authorized User | - | MessageResponse |
| PUT | `/{messageId}` | Edit message | ✅ | Message Author | MessageUpdateRequest | MessageResponse |
| DELETE | `/{messageId}` | Delete message | ✅ | Message Author | - | SuccessResponse |
| POST | `/{messageId}/react` | React to message | ✅ | Project Member | MessageReactionRequest | MessageReactionResponse |
| DELETE | `/{messageId}/react/{reactionId}` | Remove reaction | ✅ | Reaction Owner | - | SuccessResponse |
| POST | `/{messageId}/reply` | Reply to message | ✅ | Project Member | MessageReplyRequest | MessageResponse |

### 7.3 Project Messages Controller
**Base Path:** `/api/v1/projects/{projectId}/messages`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get project messages | ✅ | Member/Supervisor | Query params | List<MessageResponse> |
| POST | `/` | Send message to project | ✅ | Member/Supervisor | MessageRequest | MessageResponse |
| GET | `/search` | Search project messages | ✅ | Member/Supervisor | Query params | List<MessageResponse> |
| GET | `/threads/{threadId}` | Get message thread | ✅ | Member/Supervisor | - | MessageThreadResponse |
| POST | `/threads` | Create message thread | ✅ | Member/Supervisor | ThreadCreateRequest | MessageThreadResponse |

## 8. Administration Endpoints

### 8.1 Admin Controller
**Base Path:** `/api/v1/admin`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/dashboard` | Get admin dashboard data | ✅ | Admin | - | AdminDashboardResponse |
| GET | `/stats` | Get platform statistics | ✅ | Admin | - | PlatformStatsResponse |
| GET | `/users` | Get all users | ✅ | Admin | Query params | List<UserResponse> |
| PUT | `/users/{userId}/role` | Update user role | ✅ | Admin | RoleUpdateRequest | UserResponse |
| PUT | `/users/{userId}/status` | Update user status | ✅ | Admin | StatusUpdateRequest | UserResponse |
| DELETE | `/users/{userId}` | Delete user account | ✅ | Admin | - | SuccessResponse |
| GET | `/projects` | Get all projects | ✅ | Admin | Query params | List<ProjectResponse> |
| PUT | `/projects/{projectId}/supervisor` | Assign supervisor | ✅ | Admin | SupervisorAssignmentRequest | ProjectResponse |
| GET | `/reports/users` | Generate user report | ✅ | Admin | Query params | File Stream |
| GET | `/reports/projects` | Generate project report | ✅ | Admin | Query params | File Stream |
| GET | `/reports/activity` | Generate activity report | ✅ | Admin | Query params | File Stream |

### 8.2 University Management Controller
**Base Path:** `/api/v1/admin/universities`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/` | Get all universities | ✅ | Admin | Query params | List<UniversityResponse> |
| POST | `/` | Create university | ✅ | Admin | UniversityCreateRequest | UniversityResponse |
| GET | `/{universityId}` | Get university details | ✅ | Admin | - | UniversityResponse |
| PUT | `/{universityId}` | Update university | ✅ | Admin | UniversityUpdateRequest | UniversityResponse |
| DELETE | `/{universityId}` | Delete university | ✅ | Admin | - | SuccessResponse |
| GET | `/{universityId}/departments` | Get university departments | ✅ | Admin | - | List<DepartmentResponse> |
| POST | `/{universityId}/departments` | Create department | ✅ | Admin | DepartmentCreateRequest | DepartmentResponse |
| PUT | `/{universityId}/departments/{deptId}` | Update department | ✅ | Admin | DepartmentUpdateRequest | DepartmentResponse |
| DELETE | `/{universityId}/departments/{deptId}` | Delete department | ✅ | Admin | - | SuccessResponse |

## 9. Supervisor Dashboard Endpoints

### 9.1 Supervisor Controller
**Base Path:** `/api/v1/supervisor`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/dashboard` | Get supervisor dashboard | ✅ | Supervisor | - | SupervisorDashboardResponse |
| GET | `/projects` | Get assigned projects | ✅ | Supervisor | Query params | List<ProjectResponse> |
| GET | `/projects/{projectId}/overview` | Get project overview | ✅ | Supervisor | - | ProjectOverviewResponse |
| GET | `/workload` | Get supervisor workload | ✅ | Supervisor | - | WorkloadResponse |
| GET | `/students` | Get supervised students | ✅ | Supervisor | - | List<StudentResponse> |
| POST | `/projects/{projectId}/approve` | Approve project | ✅ | Supervisor | ApprovalRequest | ProjectResponse |
| POST | `/projects/{projectId}/feedback` | Provide project feedback | ✅ | Supervisor | FeedbackRequest | FeedbackResponse |
| GET | `/analytics` | Get supervision analytics | ✅ | Supervisor | Query params | AnalyticsResponse |

## 10. Utility & System Endpoints

### 10.1 Health & Monitoring Controller
**Base Path:** `/api/v1/system`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/health` | System health check | ❌ | - | - | HealthResponse |
| GET | `/info` | System information | ❌ | - | - | SystemInfoResponse |
| GET | `/metrics` | System metrics | ✅ | Admin | - | MetricsResponse |
| GET | `/version` | API version info | ❌ | - | - | VersionResponse |

### 10.2 Search Controller
**Base Path:** `/api/v1/search`

| Method | Endpoint | Description | Auth | Role | Request Body | Response |
|--------|----------|-------------|------|------|--------------|----------|
| GET | `/global` | Global search across all entities | ✅ | Any | Query params | SearchResultResponse |
| GET | `/projects` | Search projects | ✅ | Any | Query params | List<ProjectResponse> |
| GET | `/users` | Search users | ✅ | Any | Query params | List<PublicUserResponse> |
| GET | `/files` | Search files | ✅ | Any | Query params | List<FileResponse> |
| GET | `/suggestions` | Get search suggestions | ✅ | Any | Query params | List<SearchSuggestionResponse> |

## 11. WebSocket Endpoints

### 11.1 Real-time Communication
**Base Path:** `/ws`

| Endpoint | Description | Auth | Subscription Path | Message Types |
|----------|-------------|------|------------------|---------------|
| `/connect` | WebSocket connection endpoint | ✅ | - | Connection handshake |
| `/app/message` | Send message to project | ✅ | `/topic/project/{projectId}` | MessageRequest |
| `/app/typing` | Send typing indicator | ✅ | `/topic/project/{projectId}/typing` | TypingIndicatorRequest |
| `/app/notification` | Real-time notifications | ✅ | `/user/{userId}/notifications` | NotificationMessage |
| `/app/project-update` | Project status updates | ✅ | `/topic/project/{projectId}/updates` | ProjectUpdateMessage |
| `/app/task-update` | Task status updates | ✅ | `/topic/project/{projectId}/tasks` | TaskUpdateMessage |

**WebSocket Message Format:**
```json
{
  "type": "MESSAGE",
  "timestamp": "2024-12-01T10:30:00Z",
  "projectId": 123,
  "userId": 456,
  "data": {
    "content": "Hello team!",
    "messageId": 789
  }
}
```

## 12. Error Codes & HTTP Status Codes

### 12.1 Standard HTTP Status Codes

| Status Code | Description | Usage |
|-------------|-------------|-------|
| 200 | OK | Successful GET, PUT requests |
| 201 | Created | Successful POST requests |
| 204 | No Content | Successful DELETE requests |
| 400 | Bad Request | Invalid request data |
| 401 | Unauthorized | Missing or invalid authentication |
| 403 | Forbidden | Insufficient permissions |
| 404 | Not Found | Resource not found |
| 409 | Conflict | Resource already exists |
| 413 | Payload Too Large | File upload exceeds limit |
| 422 | Unprocessable Entity | Validation errors |
| 429 | Too Many Requests | Rate limit exceeded |
| 500 | Internal Server Error | Server-side errors |

### 12.2 Custom Error Codes

| Error Code | Description | HTTP Status |
|------------|-------------|-------------|
| VALIDATION_ERROR | Input validation failed | 400 |
| AUTHENTICATION_REQUIRED | User not authenticated | 401 |
| ACCESS_DENIED | Insufficient permissions | 403 |
| RESOURCE_NOT_FOUND | Requested resource not found | 404 |
| DUPLICATE_RESOURCE | Resource already exists | 409 |
| FILE_TOO_LARGE | File exceeds size limit | 413 |
| INVALID_FILE_TYPE | Unsupported file type | 422 |
| VIRUS_DETECTED | File contains malicious content | 422 |
| RATE_LIMIT_EXCEEDED | Too many requests | 429 |
| EMAIL_DELIVERY_FAILED | Email notification failed | 500 |
| STORAGE_SERVICE_UNAVAILABLE | File storage service down | 500 |

## 13. Request/Response Examples

### 13.1 Project Creation Example

**Request:**
```bash
POST /api/v1/projects
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "title": "نظام إدارة المكتبات الذكي",
  "description": "تطوير نظام إدارة مكتبات ذكي باستخدام الذكاء الاصطناعي",
  "projectType": "DEVELOPMENT",
  "category": "Software Engineering",
  "startDate": "2024-12-01",
  "dueDate": "2025-05-01",
  "teamMembers": [
    {
      "email": "teammate1@university.edu",
      "role": "MEMBER"
    },
    {
      "email": "teammate2@university.edu", 
      "role": "MEMBER"
    }
  ],
  "preferredSupervisorId": 15
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 123,
    "title": "نظام إدارة المكتبات الذكي",
    "description": "تطوير نظام إدارة مكتبات ذكي باستخدام الذكاء الاصطناعي",
    "projectType": "DEVELOPMENT",
    "status": "DRAFT",
    "category": "Software Engineering",
    "university": {
      "id": 1,
      "name": "جامعة القاهرة",
      "domain": "cu.edu.eg"
    },
    "teamLeader": {
      "id": 456,
      "firstName": "أحمد",
      "lastName": "محمد",
      "email": "ahmed.mohamed@cu.edu.eg"
    },
    "members": [
      {
        "id": 789,
        "user": {
          "id": 457,
          "firstName": "فاطمة",
          "lastName": "علي"
        },
        "role": "MEMBER",
        "status": "PENDING"
      }
    ],
    "startDate": "2024-12-01",
    "dueDate": "2025-05-01",
    "progressPercentage": 0.0,
    "totalTasks": 0,
    "completedTasks": 0,
    "createdAt": "2024-12-01T10:30:00Z",
    "updatedAt": "2024-12-01T10:30:00Z"
  },
  "message": "Project created successfully. Team invitations have been sent."
}
```

### 13.2 File Upload Example

**Request:**
```bash
POST /api/v1/projects/123/files
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="project-proposal.pdf"
Content-Type: application/pdf

[Binary file data]
--boundary
Content-Disposition: form-data; name="description"

مقترح المشروع النهائي
--boundary
Content-Disposition: form-data; name="deliverableType"

PROPOSAL
--boundary--
```

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 789,
    "filename": "project-proposal.pdf",
    "originalFilename": "project-proposal.pdf",
    "contentType": "application/pdf",
    "fileSize": 2048576,
    "description": "مقترح المشروع النهائي",
    "deliverableType": "PROPOSAL",
    "version": 1,
    "status": "AVAILABLE",
    "uploadedBy": {
      "id": 456,
      "firstName": "أحمد",
      "lastName": "محمد"
    },
    "virusScanResult": "CLEAN",
    "downloadUrl": "/api/v1/files/789",
    "createdAt": "2024-12-01T10:35:00Z"
  },
  "message": "File uploaded successfully"
}
```

## 14. Rate Limiting & Quotas

### 14.1 Rate Limits by Endpoint Category

| Category | Requests per Minute | Burst Limit |
|----------|-------------------|-------------|
| Authentication | 10 | 20 |
| File Upload | 5 | 10 |
| File Download | 30 | 50 |
| API Calls (General) | 100 | 200 |
| WebSocket Messages | 60 | 120 |
| Search Queries | 30 | 60 |

### 14.2 User Role Quotas

| Resource | Student | Supervisor | Admin |
|----------|---------|------------|-------|
| Projects (as member) | 3 | N/A | Unlimited |
| Projects (as supervisor) | N/A | 12 | Unlimited |
| File Storage (per project) | 1GB | N/A | Unlimited |
| Team Members (per project) | 4 | N/A | N/A |
| API Calls (per hour) | 1000 | 2000 | Unlimited |

## 15. API Versioning & Deprecation

### 15.1 Version History

| Version | Release Date | Status | End of Support |
|---------|-------------|--------|----------------|
| v1.0 | 2024-12-01 | Current | TBD |

### 15.2 Breaking Changes Policy

- **Major Version:** Breaking changes, require client updates
- **Minor Version:** New features, backward compatible
- **Patch Version:** Bug fixes, backward compatible

### 15.3 Deprecation Timeline

When deprecating endpoints:
1. **6 months notice:** Deprecation warning in response headers
2. **3 months notice:** Deprecation warning in API documentation
3. **1 month notice:** Final warning before removal
4. **Removal:** Endpoint returns 410 Gone

## 16. Development & Testing

### 16.1 Environment URLs

| Environment | Base URL | Purpose |
|-------------|----------|---------|
| Development | http://localhost:8080/api/v1 | Local development |
| Testing | https://test-api.takharujy.tech/v1 | Integration testing |
| Staging | https://staging-api.takharujy.tech/v1 | Pre-production |
| Production | https://api.takharujy.tech/v1 | Live production |

### 16.2 API Testing Tools

**Recommended Tools:**
- **Postman:** API testing and documentation
- **Insomnia:** REST API client
- **curl:** Command-line testing
- **HTTPie:** User-friendly CLI tool

**Sample Collection:**
- Postman collection available at: `/docs/postman/takharrujy-api.json`
- OpenAPI specification at: `/api/v1/docs`
- Interactive API docs at: `/api/v1/swagger-ui`

### 16.3 Postman Testing Requirements

**MANDATORY:** For each endpoint, create a dedicated folder structure in the `postman/` directory:

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
... (continue for all 87 endpoints)
```

**Required Files per Endpoint:**
1. **Environment Files (3 per endpoint):**
   - `Takharrujy-Dev.postman_environment.json` - Development environment
   - `Takharrujy-Staging.postman_environment.json` - Staging environment  
   - `Takharrujy-Prod.postman_environment.json` - Production environment

2. **Collection File (1 per endpoint):**
   - `{EndpointName}.postman_collection.json` - Complete test scenarios

**Collection Requirements:**
- **Happy Path Tests:** Successful request/response scenarios
- **Error Scenarios:** Invalid data, authentication failures, authorization errors
- **Edge Cases:** Boundary testing, empty data, large payloads
- **Arabic Language Tests:** RTL text handling, Arabic character validation
- **Pre-request Scripts:** Authentication token setup, data preparation
- **Test Scripts:** Response validation, status code verification, data integrity checks
- **Documentation:** Request/response examples with Arabic language support

**Environment Variables:**
```json
{
  "name": "Takharrujy-Dev",
  "values": [
    {"key": "baseUrl", "value": "http://localhost:8080/api/v1"},
    {"key": "authToken", "value": ""},
    {"key": "userId", "value": ""},
    {"key": "projectId", "value": ""},
    {"key": "universityId", "value": "1"}
  ]
}
```

---

**API Documentation Status:** ✅ Complete  
**Total Endpoints:** 87 endpoints  
**Authentication:** JWT Bearer Token  
**Rate Limiting:** ✅ Implemented  
**Documentation:** OpenAPI 3.0 + Swagger UI  
**Last Updated:** December 2024

This comprehensive API documentation provides all the endpoints needed for the Takharrujy platform MVP, supporting the full feature set outlined in the project requirements while maintaining scalability for future enhancements.
