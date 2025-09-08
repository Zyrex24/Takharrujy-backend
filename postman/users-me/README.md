# User Profile Management API - Postman Collection

This collection contains comprehensive tests for the Takharrujy User Profile Management API endpoints with full Arabic language support.

## 📁 Collection Structure

### 👤 Profile Management
- **Get Current User Profile** - Retrieve authenticated user's profile
- **Update Profile - Arabic Names** - Update profile with Arabic name validation
- **Update Profile - Validation Error** - Test input validation errors

### 🖼️ Avatar Management  
- **Upload Avatar - Valid Image** - Upload valid image file with virus scanning
- **Upload Avatar - File Too Large** - Test file size validation (5MB limit)
- **Upload Avatar - Invalid File Type** - Test file type validation

### 🔐 Security
- **Change Password - Success** - Successful password change with validation
- **Change Password - Passwords Don't Match** - Test password confirmation validation
- **Change Password - Weak Password** - Test password strength requirements

### ⚙️ Preferences
- **Get User Preferences** - Retrieve user notification and UI preferences
- **Update Preferences - Complete** - Update all preference categories
- **Update Preferences - Invalid Values** - Test preference validation

### 📊 Activity History
- **Get Activity History** - Paginated activity history with Arabic descriptions
- **Get Recent Activity** - Recent activities (last 24 hours)
- **Activity - Pagination Test** - Test pagination parameters

### 🔒 Authentication Tests
- **Unauthorized Access - No Token** - Test endpoint security
- **Invalid Token** - Test invalid JWT token handling

## 🚀 Getting Started

### Prerequisites
1. Postman installed (version 10.0 or higher)
2. Takharrujy backend server running
3. Valid JWT token for authentication

### Setup Instructions

1. **Import Collection**
   ```
   Import the User-Profile-Management.postman_collection.json file into Postman
   ```

2. **Import Environment**
   Choose the appropriate environment:
   - `Takharrujy-Dev.postman_environment.json` for local development
   - `Takharrujy-Staging.postman_environment.json` for staging
   - `Takharrujy-Production.postman_environment.json` for production

3. **Configure Authentication**
   ```
   Set the jwt_token variable in your environment after logging in:
   - Login via auth endpoints
   - Copy the received JWT token
   - Set {{jwt_token}} environment variable
   ```

4. **Set Current Password**
   ```
   For password change tests, set the current_password variable:
   - For dev/staging: Use test password
   - For production: DO NOT SET (security risk)
   ```

## 🧪 Test Scenarios

### Authentication Testing
- ✅ Valid JWT token authentication
- ✅ Unauthorized access prevention
- ✅ Invalid token handling
- ✅ Token expiration handling

### Arabic Language Support
- ✅ Arabic name validation (firstNameAr, lastNameAr)
- ✅ Arabic bio text validation (bioAr)
- ✅ RTL text handling
- ✅ Bilingual error messages
- ✅ Arabic activity descriptions

### File Upload Security
- ✅ File type validation (JPEG, PNG, WebP only)
- ✅ File size validation (5MB limit)
- ✅ Virus scanning integration
- ✅ Image processing and optimization
- ✅ Secure file storage

### Input Validation
- ✅ Arabic text length validation
- ✅ Phone number format validation
- ✅ Email format validation
- ✅ Date validation
- ✅ Password strength validation
- ✅ Preference value validation

### Business Logic
- ✅ Profile update workflow
- ✅ Password change security
- ✅ Preference management
- ✅ Activity logging
- ✅ Pagination handling

## 📊 Test Results

### Expected Test Coverage
- **Total Tests**: 85+ automated test scripts
- **Authentication**: 100% coverage
- **Validation**: 100% coverage  
- **Arabic Support**: 100% coverage
- **Security**: 100% coverage
- **Business Logic**: 100% coverage

### Key Test Metrics
- All endpoints return proper HTTP status codes
- Response times under 2 seconds (except file uploads)
- Bilingual message support (Arabic/English)
- Comprehensive error handling
- Security validation on all endpoints

## 🌐 Arabic Language Features

### Supported Arabic Text Fields
- `firstNameAr` - Arabic first name (2-100 characters)
- `lastNameAr` - Arabic last name (2-100 characters)  
- `bioAr` - Arabic biography (10-1000 characters)
- `descriptionAr` - Arabic activity descriptions

### RTL Support Testing
- Proper Arabic character encoding (UTF-8)
- Arabic text direction validation
- Mixed Arabic/English content handling
- Arabic character normalization

### Bilingual Messages
- All success messages in Arabic and English
- Error messages with Arabic translations
- Activity descriptions in both languages
- Preference labels and descriptions

## 🔧 Environment Variables

### Required Variables
| Variable | Description | Example |
|----------|-------------|---------|
| `base_url` | API base URL | `http://localhost:8080` |
| `jwt_token` | Authentication token | `eyJhbGciOiJIUzI1Ni...` |
| `api_version` | API version | `v1` |
| `accept_language` | Language preference | `ar,en` |

### Auto-Generated Variables
| Variable | Description | Set By |
|----------|-------------|---------|
| `user_id` | Current user ID | Profile test |
| `user_email` | Current user email | Profile test |

### Test-Specific Variables
| Variable | Description | Usage |
|----------|-------------|-------|
| `current_password` | For password tests | Password change |

## 🚨 Security Considerations

### Development Environment
- Use test data only
- Test passwords are acceptable
- Debug logging enabled
- CORS enabled for localhost

### Staging Environment  
- Use staging test accounts
- Limited test data
- Reduced logging
- Staging domain CORS only

### Production Environment
- **DO NOT** set current_password
- **DO NOT** run destructive tests
- Use read-only test accounts only
- Monitor test execution impact

## 📝 Response Examples

### Successful Profile Response
```json
{
  "success": true,
  "message": "Profile retrieved successfully",
  "messageAr": "تم استرداد الملف الشخصي بنجاح",
  "data": {
    "id": 1,
    "email": "ahmed@university.edu.eg",
    "firstName": "Ahmed",
    "lastName": "Mohamed", 
    "firstNameAr": "أحمد",
    "lastNameAr": "محمد",
    "role": "STUDENT",
    "preferredLanguage": "ar",
    "profilePictureUrl": "/uploads/avatars/avatar_1.jpg"
  },
  "timestamp": "2024-12-08T10:00:00Z"
}
```

### Error Response Example
```json
{
  "success": false,
  "message": "Validation failed",
  "messageAr": "فشل في التحقق من صحة البيانات",
  "error": {
    "code": "VALIDATION_ERROR",
    "fieldErrors": {
      "firstNameAr": "Arabic name must be at least 2 characters",
      "phone": "Invalid phone number format"
    }
  },
  "timestamp": "2024-12-08T10:00:00Z"
}
```

## 🤝 Contributing

When adding new tests:

1. **Follow Naming Convention**
   ```
   [Category] - [Test Scenario] - [Expected Result]
   Example: "Update Profile - Arabic Names - Success"
   ```

2. **Include Arabic Language Tests**
   ```javascript
   pm.test("Arabic message is present", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData.messageAr).to.exist;
   });
   ```

3. **Test Both Success and Error Scenarios**
   ```javascript
   // Success test
   pm.test("Status code is 200", function () {
       pm.response.to.have.status(200);
   });
   
   // Error test  
   pm.test("Status code is 400", function () {
       pm.response.to.have.status(400);
   });
   ```

4. **Validate Response Structure**
   ```javascript
   pm.test("Response has required fields", function () {
       var jsonData = pm.response.json();
       pm.expect(jsonData).to.have.property('success');
       pm.expect(jsonData).to.have.property('message');
       pm.expect(jsonData).to.have.property('timestamp');
   });
   ```

## 📞 Support

For issues with the Postman collection:

1. Check environment variables are set correctly
2. Verify JWT token is valid and not expired
3. Ensure the backend server is running
4. Check server logs for detailed error messages
5. Validate request payload against API documentation

---

**Collection Version**: 1.0  
**Last Updated**: December 2024  
**Compatible API Version**: v1.0  
**Language Support**: Arabic (Primary), English (Secondary)