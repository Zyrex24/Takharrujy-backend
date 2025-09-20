package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.*;
import com.university.takharrujy.domain.enums.ProjectStatus;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.*;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.project.*;
import com.university.takharrujy.presentation.mapper.ProjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProjectService
 * Tests project management functionality
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Project Service Tests")
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectService projectService;

    private User testStudent;
    private User testSupervisor;
    private User testAdmin;
    private Project testProject;
    private CreateProjectRequest testCreateRequest;
    private ProjectResponse testProjectResponse;

    @BeforeEach
    void setUp() {
        // Setup test student
        testStudent = new User();
        testStudent.setId(1L);
        testStudent.setEmail("student@university.edu");
        testStudent.setFirstName("Ahmed");
        testStudent.setLastName("Mohamed");
        testStudent.setFirstNameAr("أحمد");
        testStudent.setLastNameAr("محمد");
        testStudent.setRole(UserRole.STUDENT);
        testStudent.setUniversityId(1L);
        testStudent.setIsActive(true);

        // Setup test supervisor
        testSupervisor = new User();
        testSupervisor.setId(2L);
        testSupervisor.setEmail("supervisor@university.edu");
        testSupervisor.setFirstName("Dr. Sara");
        testSupervisor.setLastName("Ali");
        testSupervisor.setFirstNameAr("د. سارة");
        testSupervisor.setLastNameAr("علي");
        testSupervisor.setRole(UserRole.SUPERVISOR);
        testSupervisor.setUniversityId(1L);
        testSupervisor.setIsActive(true);

        // Setup test admin
        testAdmin = new User();
        testAdmin.setId(3L);
        testAdmin.setEmail("admin@university.edu");
        testAdmin.setFirstName("Admin");
        testAdmin.setLastName("User");
        testAdmin.setFirstNameAr("مدير");
        testAdmin.setLastNameAr("المستخدم");
        testAdmin.setRole(UserRole.ADMIN);
        testAdmin.setUniversityId(1L);
        testAdmin.setIsActive(true);

        // Setup test project
        testProject = new Project();
        testProject.setId(1L);
        testProject.setTitle("AI Research Project");
        testProject.setTitleAr("مشروع بحث الذكاء الاصطناعي");
        testProject.setDescription("A comprehensive AI research project");
        testProject.setDescriptionAr("مشروع بحث شامل في الذكاء الاصطناعي");
        testProject.setStartDate(LocalDate.now().plusDays(1));
        testProject.setDueDate(LocalDate.now().plusDays(30));
        testProject.setTeamLeader(testStudent);
        testProject.setUniversityId(1L);
        testProject.setStatus(ProjectStatus.DRAFT);

        // Setup test create request
        testCreateRequest = new CreateProjectRequest(
            "AI Research Project",
            "مشروع بحث الذكاء الاصطناعي",
            "A comprehensive AI research project",
            "مشروع بحث شامل في الذكاء الاصطناعي",
            Arrays.asList(4L, 5L),
            2L,
            1L,
            1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(30),
            false
        );

        // Setup test project response
        testProjectResponse = new ProjectResponse(
            1L,
            "AI Research Project",
            "مشروع بحث الذكاء الاصطناعي",
            "A comprehensive AI research project",
            "مشروع بحث شامل في الذكاء الاصطناعي",
            ProjectStatus.DRAFT,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(30),
            null, // completionDate
            null, // progressPercentage
            null, // finalGrade
            null, // teamLeader
            null, // supervisor
            null, // teamMembers
            null, // facultyName
            null, // categoryName
            null, // createdAt
            null  // updatedAt
        );
    }

    // Create Project Tests

    @Test
    @DisplayName("Should create project successfully")
    void shouldCreateProjectSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(4L)).thenReturn(Optional.of(createTestUser(4L, "Member1", UserRole.STUDENT)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(createTestUser(5L, "Member2", UserRole.STUDENT)));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
        when(projectRepository.hasActiveProject(4L)).thenReturn(false);
        when(projectRepository.hasActiveProject(5L)).thenReturn(false);
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);
        when(projectMapper.toProjectResponse(any(Project.class))).thenReturn(testProjectResponse);

        // When
        ProjectResponse result = projectService.createProject(testCreateRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("AI Research Project");
        assertThat(result.titleAr()).isEqualTo("مشروع بحث الذكاء الاصطناعي");
        
        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(projectRepository).save(any(Project.class));
        verify(projectMapper).toProjectResponse(any(Project.class));
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when user is not a student")
    void shouldThrowExceptionWhenUserNotStudent() {
        // Given
        testStudent.setRole(UserRole.SUPERVISOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only students can create projects");

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(projectRepository, projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when user already has active project")
    void shouldThrowExceptionWhenUserHasActiveProject() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("You already have an active project");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when team member not found")
    void shouldThrowExceptionWhenTeamMemberNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(4L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(userRepository).findById(4L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when team member is not a student")
    void shouldThrowExceptionWhenTeamMemberNotStudent() {
        // Given
        User nonStudent = createTestUser(4L, "NonStudent", UserRole.SUPERVISOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(4L)).thenReturn(Optional.of(nonStudent));

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Team members must be students");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(userRepository).findById(4L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when team member already has active project")
    void shouldThrowExceptionWhenTeamMemberHasActiveProject() {
        // Given
        User teamMember = createTestUser(4L, "Member1", UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(4L)).thenReturn(Optional.of(teamMember));
        when(projectRepository.hasActiveProject(4L)).thenReturn(true);

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already has an active project");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(userRepository).findById(4L);
        verify(projectRepository).hasActiveProject(4L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when trying to add self as team member")
    void shouldThrowExceptionWhenAddingSelfAsTeamMember() {
        // Given
        CreateProjectRequest requestWithSelf = new CreateProjectRequest(
            "AI Research Project",
            "مشروع بحث الذكاء الاصطناعي",
            "A comprehensive AI research project",
            "مشروع بحث شامل في الذكاء الاصطناعي",
            Arrays.asList(1L), // Adding self as team member
            2L,
            1L,
            1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(30),
            false
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(requestWithSelf, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Cannot add yourself as a team member");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when team size exceeds limit")
    void shouldThrowExceptionWhenTeamSizeExceedsLimit() {
        // Given
        CreateProjectRequest requestWithTooManyMembers = new CreateProjectRequest(
            "AI Research Project",
            "مشروع بحث الذكاء الاصطناعي",
            "A comprehensive AI research project",
            "مشروع بحث شامل في الذكاء الاصطناعي",
            Arrays.asList(4L, 5L, 6L, 7L), // 4 additional members (exceeds limit of 3)
            2L,
            1L,
            1L,
            LocalDate.now().plusDays(1),
            LocalDate.now().plusDays(30),
            false
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(requestWithTooManyMembers, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Maximum 3 additional team members allowed");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when preferred supervisor not found")
    void shouldThrowExceptionWhenPreferredSupervisorNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("User not found");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when preferred supervisor is not a supervisor")
    void shouldThrowExceptionWhenPreferredSupervisorNotSupervisor() {
        // Given
        User nonSupervisor = createTestUser(2L, "NonSupervisor", UserRole.STUDENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(nonSupervisor));

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Selected user is not a supervisor");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when supervisor is from different university")
    void shouldThrowExceptionWhenSupervisorFromDifferentUniversity() {
        // Given
        testSupervisor.setUniversityId(2L); // Different university
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(testCreateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Supervisor must be from the same university");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verify(userRepository).findById(2L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when date range is invalid")
    void shouldThrowExceptionWhenDateRangeInvalid() {
        // Given
        CreateProjectRequest invalidDateRequest = new CreateProjectRequest(
            "AI Research Project",
            "مشروع بحث الذكاء الاصطناعي",
            "A comprehensive AI research project",
            "مشروع بحث شامل في الذكاء الاصطناعي",
            Arrays.asList(),
            null,
            1L,
            1L,
            LocalDate.now().plusDays(30), // Start date after end date
            LocalDate.now().plusDays(1),
            false
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectRepository.hasActiveProject(1L)).thenReturn(false);

        // When/Then
        assertThatThrownBy(() -> projectService.createProject(invalidDateRequest, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("End date must be after start date");

        verify(userRepository).findById(1L);
        verify(projectRepository).hasActiveProject(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    // Helper method to create test users
    private User createTestUser(Long id, String name, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setEmail(name.toLowerCase() + "@university.edu");
        user.setFirstName(name);
        user.setLastName("User");
        user.setFirstNameAr(name);
        user.setLastNameAr("مستخدم");
        user.setRole(role);
        user.setUniversityId(1L);
        user.setIsActive(true);
        return user;
    }
}
