# Testing Automation Commands

## Testing Framework Setup
- **Unit Testing:** JUnit 5 + Mockito
- **Integration Testing:** Spring Boot Test + Testcontainers
- **Performance Testing:** K6
- **Security Testing:** OWASP ZAP
- **API Testing:** REST Assured
- **Coverage:** JaCoCo

## Unit Testing Commands

### 1. Service Layer Testing
```java
@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {
    
    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @InjectMocks
    private ProjectService projectService;
    
    @Test
    @DisplayName("Should create project successfully")
    void shouldCreateProjectSuccessfully() {
        // Given
        CreateProjectRequest request = CreateProjectRequest.builder()
            .title("مشروع التخرج")
            .description("وصف المشروع")
            .build();
        
        // When & Then
        assertDoesNotThrow(() -> projectService.createProject(request));
    }
}
```

### 2. Controller Testing
```java
@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ProjectService projectService;
    
    @Test
    @WithMockUser(roles = "STUDENT")
    void shouldCreateProjectWithValidData() throws Exception {
        mockMvc.perform(post("/api/v1/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "مشروع التخرج",
                        "description": "وصف المشروع"
                    }
                    """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }
}
```

## Integration Testing Commands

### 1. Database Integration Tests
```java
@SpringBootTest
@Testcontainers
@Transactional
class ProjectRepositoryIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("takharrujy_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Test
    void shouldFindProjectsByUniversityId() {
        // Test implementation
    }
}
```

### 2. Redis Integration Tests
```java
@SpringBootTest
@Testcontainers
class RedisCacheIntegrationTest {
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);
    
    @Test
    void shouldCacheProjectData() {
        // Test caching behavior
    }
}
```

## API Testing Commands

### 1. REST Assured Tests
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProjectApiIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Test
    void shouldCreateProjectViaApi() {
        given()
            .contentType(ContentType.JSON)
            .header("Authorization", "Bearer " + getValidJwtToken())
            .body("""
                {
                    "title": "مشروع التخرج",
                    "description": "وصف المشروع"
                }
                """)
        .when()
            .post("/api/v1/projects")
        .then()
            .statusCode(201)
            .body("success", equalTo(true))
            .body("data.title", equalTo("مشروع التخرج"));
    }
}
```

## Performance Testing Commands

### 1. K6 Load Tests
```javascript
// k6-load-test.js
import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '2m', target: 100 },
        { duration: '5m', target: 100 },
        { duration: '2m', target: 200 },
        { duration: '5m', target: 200 },
        { duration: '2m', target: 0 }
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],
        http_req_failed: ['rate<0.1']
    }
};

export default function() {
    const payload = JSON.stringify({
        title: 'مشروع اختبار الأداء',
        description: 'وصف مشروع اختبار الأداء'
    });
    
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + __ENV.JWT_TOKEN
        }
    };
    
    const response = http.post('http://localhost:8080/api/v1/projects', payload, params);
    
    check(response, {
        'status is 201': (r) => r.status === 201,
        'response time < 500ms': (r) => r.timings.duration < 500
    });
    
    sleep(1);
}
```

### 2. Database Performance Tests
```java
@Test
@Timeout(value = 5, unit = TimeUnit.SECONDS)
void shouldHandleConcurrentProjectCreation() {
    int numberOfThreads = 50;
    ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    
    List<CompletableFuture<Void>> futures = IntStream.range(0, numberOfThreads)
        .mapToObj(i -> CompletableFuture.runAsync(() -> {
            CreateProjectRequest request = CreateProjectRequest.builder()
                .title("مشروع متزامن " + i)
                .build();
            projectService.createProject(request);
        }, executor))
        .collect(Collectors.toList());
    
    assertDoesNotThrow(() -> 
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get());
}
```

## Security Testing Commands

### 1. Authentication Tests
```java
@Test
void shouldRejectRequestWithoutAuthentication() throws Exception {
    mockMvc.perform(post("/api/v1/projects")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isUnauthorized());
}

@Test
void shouldRejectRequestWithInvalidRole() throws Exception {
    mockMvc.perform(post("/api/v1/projects")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_GUEST")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
            .andExpect(status().isForbidden());
}
```

### 2. Input Validation Tests
```java
@ParameterizedTest
@ValueSource(strings = {"", " ", "a".repeat(201)})
void shouldRejectInvalidProjectTitles(String title) throws Exception {
    mockMvc.perform(post("/api/v1/projects")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STUDENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(String.format("""
                {
                    "title": "%s",
                    "description": "Valid description"
                }
                """, title)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
}
```

## Test Execution Commands

```bash
# Run all tests
mvn clean test

# Run specific test categories
mvn test -Dgroups="unit"
mvn test -Dgroups="integration"
mvn test -Dgroups="performance"

# Run tests with coverage
mvn clean test jacoco:report

# Run K6 performance tests
k6 run --env JWT_TOKEN=$JWT_TOKEN k6-load-test.js

# Run security tests with OWASP ZAP
zap-baseline.py -t http://localhost:8080 -J zap-report.json

# Continuous testing
mvn test -Dcontinuous=true
```

## Test Data Management

### 1. Test Fixtures
```java
@Component
public class TestDataFactory {
    
    public static CreateProjectRequest validProjectRequest() {
        return CreateProjectRequest.builder()
            .title("مشروع اختبار")
            .description("وصف مشروع الاختبار")
            .build();
    }
    
    public static User createTestStudent() {
        return User.builder()
            .email("student@university.edu.sa")
            .name("أحمد محمد")
            .role(UserRole.STUDENT)
            .build();
    }
}
```

### 2. Database Cleanup
```java
@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup("classpath:test-data.xml")
@DatabaseTearDown(type = DatabaseOperation.DELETE_ALL, value = "classpath:cleanup.xml")
class DatabaseTest {
    // Test implementation
}
```

## Automated Test Reports

```xml
<!-- Maven Surefire Plugin Configuration -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*Tests.java</include>
        </includes>
        <excludes>
            <exclude>**/*IntegrationTest.java</exclude>
        </excludes>
        <systemPropertyVariables>
            <spring.profiles.active>test</spring.profiles.active>
        </systemPropertyVariables>
    </configuration>
</plugin>
```

## Test Quality Gates

```yaml
# Quality gates for CI/CD
quality_gates:
  unit_test_coverage: 80%
  integration_test_coverage: 70%
  performance_threshold: 500ms
  security_scan_threshold: 0
  code_duplication: <5%
```
