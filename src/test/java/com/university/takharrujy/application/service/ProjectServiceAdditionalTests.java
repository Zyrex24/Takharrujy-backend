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
import static org.mockito.Mockito.*;

/**
 * Additional unit tests for ProjectService
 * Tests dashboard, get project, submit project, and update project methods
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Project Service Additional Tests")
class ProjectServiceAdditionalTests {

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
    private Project testProject;
    private CreateProjectRequest testUpdateRequest;

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
        testProject.setSupervisor(testSupervisor);
        testProject.setUniversityId(1L);
        testProject.setStatus(ProjectStatus.DRAFT);

        // Setup test update request
        testUpdateRequest = new CreateProjectRequest(
            "Updated AI Research Project",
            "مشروع بحث الذكاء الاصطناعي المحدث",
            "An updated comprehensive AI research project",
            "مشروع بحث شامل محدث في الذكاء الاصطناعي",
            Arrays.asList(4L, 5L),
            2L,
            1L,
            1L,
            LocalDate.now().plusDays(2),
            LocalDate.now().plusDays(35),
            false
        );
    }

    // Dashboard Tests

    @Test
    @DisplayName("Should return dashboard with current project")
    void shouldReturnDashboardWithCurrentProject() {
        // Given
        when(projectRepository.findCurrentProjectByUserId(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.countActiveProjectsByUserId(1L)).thenReturn(1L);

        // When
        DashboardResponse result = projectService.getCurrentProjectDashboard(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.project()).isNotNull();
        assertThat(result.project().title()).isEqualTo("AI Research Project");
        assertThat(result.project().titleAr()).isEqualTo("مشروع بحث الذكاء الاصطناعي");
        assertThat(result.project().status()).isEqualTo("Draft");
        assertThat(result.project().supervisorName()).isEqualTo("Dr. Sara Ali");
        assertThat(result.project().supervisorNameAr()).isEqualTo("د. سارة علي");

        verify(projectRepository).findCurrentProjectByUserId(1L);
        verify(projectRepository).countActiveProjectsByUserId(1L);
    }

    @Test
    @DisplayName("Should return empty dashboard when no current project")
    void shouldReturnEmptyDashboardWhenNoCurrentProject() {
        // Given
        when(projectRepository.findCurrentProjectByUserId(1L)).thenReturn(Optional.empty());
        when(projectRepository.countActiveProjectsByUserId(1L)).thenReturn(0L);

        // When
        DashboardResponse result = projectService.getCurrentProjectDashboard(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.project()).isNull();
        assertThat(result.counters().activeProjects()).isEqualTo(0);

        verify(projectRepository).findCurrentProjectByUserId(1L);
        verify(projectRepository).countActiveProjectsByUserId(1L);
    }

    // Get Project Tests

    @Test
    @DisplayName("Should return project when user has access")
    void shouldReturnProjectWhenUserHasAccess() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(createProjectResponse());

        // When
        ProjectResponse result = projectService.getProject(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("AI Research Project");
        assertThat(result.titleAr()).isEqualTo("مشروع بحث الذكاء الاصطناعي");

        verify(projectRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    @DisplayName("Should throw exception when project not found")
    void shouldThrowExceptionWhenProjectNotFound() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> projectService.getProject(1L, 1L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Project not found");

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(userRepository, projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when user has no access to project")
    void shouldThrowExceptionWhenUserHasNoAccess() {
        // Given
        User otherUser = createTestUser(99L, "OtherUser", UserRole.STUDENT);
        otherUser.setUniversityId(2L); // Different university

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(99L)).thenReturn(Optional.of(otherUser));

        // When/Then
        assertThatThrownBy(() -> projectService.getProject(1L, 99L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("You don't have access to this project");

        verify(projectRepository).findById(1L);
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should allow supervisor access to assigned project")
    void shouldAllowSupervisorAccessToAssignedProject() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(2L)).thenReturn(Optional.of(testSupervisor));
        when(projectMapper.toProjectResponse(testProject)).thenReturn(createProjectResponse());

        // When
        ProjectResponse result = projectService.getProject(1L, 2L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("AI Research Project");

        verify(projectRepository).findById(1L);
        verify(userRepository).findById(2L);
        verify(projectMapper).toProjectResponse(testProject);
    }

    // Submit Project Tests

    @Test
    @DisplayName("Should submit project successfully")
    void shouldSubmitProjectSuccessfully() {
        // Given
        testProject.setStatus(ProjectStatus.DRAFT);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toProjectResponse(testProject)).thenReturn(createProjectResponse());

        // When
        ProjectResponse result = projectService.submitProject(1L, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(testProject.getStatus()).isEqualTo(ProjectStatus.SUBMITTED);

        verify(projectRepository).findById(1L);
        verify(projectRepository).save(testProject);
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    @DisplayName("Should throw exception when only team leader can submit")
    void shouldThrowExceptionWhenOnlyTeamLeaderCanSubmit() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When/Then
        assertThatThrownBy(() -> projectService.submitProject(1L, 99L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only team leader can submit the project");

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when project is not in draft status")
    void shouldThrowExceptionWhenProjectNotInDraftStatus() {
        // Given
        testProject.setStatus(ProjectStatus.SUBMITTED);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When/Then
        assertThatThrownBy(() -> projectService.submitProject(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Only draft projects can be submitted");

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when project title is missing for submission")
    void shouldThrowExceptionWhenProjectTitleMissingForSubmission() {
        // Given
        testProject.setTitle(null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When/Then
        assertThatThrownBy(() -> projectService.submitProject(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Project title is required for submission");

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when project description is too short for submission")
    void shouldThrowExceptionWhenProjectDescriptionTooShortForSubmission() {
        // Given
        testProject.setDescription("Short"); // Less than 50 characters
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When/Then
        assertThatThrownBy(() -> projectService.submitProject(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Project description must be at least 50 characters for submission");

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    @Test
    @DisplayName("Should throw exception when project dates are missing for submission")
    void shouldThrowExceptionWhenProjectDatesMissingForSubmission() {
        // Given
        testProject.setStartDate(null);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        // When/Then
        assertThatThrownBy(() -> projectService.submitProject(1L, 1L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Start and end dates are required for submission");

        verify(projectRepository).findById(1L);
        verifyNoMoreInteractions(projectMapper);
    }

    // Update Project Tests

    @Test
    @DisplayName("Should update project successfully")
    void shouldUpdateProjectSuccessfully() {
        // Given
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testStudent));
        when(userRepository.findById(4L)).thenReturn(Optional.of(createTestUser(4L, "Member1", UserRole.STUDENT)));
        when(userRepository.findById(5L)).thenReturn(Optional.of(createTestUser(5L, "Member2", UserRole.STUDENT)));
        when(projectRepository.save(testProject)).thenReturn(testProject);
        when(projectMapper.toProjectResponse(testProject)).thenReturn(createProjectResponse());

        // When
        ProjectResponse result = projectService.updateProject(1L, testUpdateRequest, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(testProject.getTitle()).isEqualTo("Updated AI Research Project");
        assertThat(testProject.getTitleAr()).isEqualTo("مشروع بحث الذكاء الاصطناعي المحدث");

        verify(projectRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(projectRepository).save(testProject);
        verify(projectMapper).toProjectResponse(testProject);
    }

    @Test
    @DisplayName("Should throw exception when user has no permission to edit project")
    void shouldThrowExceptionWhenUserHasNoPermissionToEdit() {
        // Given
        User otherUser = createTestUser(99L, "OtherUser", UserRole.STUDENT);
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(userRepository.findById(99L)).thenReturn(Optional.of(otherUser));

        // When/Then
        assertThatThrownBy(() -> projectService.updateProject(1L, testUpdateRequest, 99L))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("You don't have permission to edit this project");

        verify(projectRepository).findById(1L);
        verify(userRepository).findById(99L);
        verifyNoMoreInteractions(projectMapper);
    }

    // Helper methods

    private ProjectResponse createProjectResponse() {
        return new ProjectResponse(
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
