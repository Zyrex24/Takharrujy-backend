package com.university.takharrujy.application.service;

import com.university.takharrujy.domain.entity.User;
import com.university.takharrujy.domain.enums.NotificationType;
import com.university.takharrujy.domain.enums.UserRole;
import com.university.takharrujy.domain.repository.DeliverableRepository;
import com.university.takharrujy.domain.repository.ProjectRepository;
import com.university.takharrujy.domain.repository.UniversityRepository;
import com.university.takharrujy.domain.repository.UserRepository;
import com.university.takharrujy.infrastructure.exception.BusinessException;
import com.university.takharrujy.infrastructure.exception.ResourceNotFoundException;
import com.university.takharrujy.presentation.dto.admin.AdminDashboardResponse;
import com.university.takharrujy.presentation.dto.admin.RoleUpdateRequest;
import com.university.takharrujy.presentation.dto.user.UserResponse;
import com.university.takharrujy.presentation.mapper.UserMapper;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final DeliverableRepository deliverableRepository;
    private final UniversityRepository universityRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    public AdminService(ProjectRepository projectRepository, UserRepository userRepository, DeliverableRepository deliverableRepository, UniversityRepository universityRepository, UserMapper userMapper, NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.deliverableRepository = deliverableRepository;
        this.universityRepository = universityRepository;
        this.userMapper = userMapper;
        this.notificationService = notificationService;
    }

    // ---------- Dashboard ----------
    @Transactional
    public AdminDashboardResponse getDashboard() {
        return new AdminDashboardResponse(
                projectRepository.count(),
                userRepository.count(),
                userRepository.countByRole(UserRole.SUPERVISOR),
                deliverableRepository.count(),
                universityRepository.count()
        );
    }

    // ---------- User Management ----------
    @Transactional
    public List<UserResponse> getAllUsersForAdmin() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, RoleUpdateRequest request) {
        if (request == null || request.role() == null) {
            throw BusinessException.invalidInput("Role cannot be null");
        }

        User user = getUserOrThrow(userId);

        user.setRole(request.role());
        User updatedUser = userRepository.save(user);

        // Notification
        notificationService.createNotification(
                user,
                "Role Updated",
                "Your role has been changed to " + updatedUser.getRole(),
                NotificationType.USER
        );

        return userMapper.toResponse(updatedUser);
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, boolean isActive) {
        User user = getUserOrThrow(userId);
        ensureNotAdmin(user, "deactivate");

        // No-op if already same status
        if (Boolean.TRUE.equals(user.getIsActive()) == isActive) {
            return userMapper.toResponse(user);
        }

        user.setIsActive(isActive);

        // Notification
        String statusText = isActive ? "activated" : "deactivated";
        notificationService.createNotification(
                user,
                "Account Status Changed",
                "Your account has been " + statusText + " by an admin.",
                NotificationType.USER
        );

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserOrThrow(userId);
        ensureNotAdmin(user, "delete");

        try {
            // Soft delete (mark inactive)
            user.setIsActive(false);
            userRepository.save(user);

            // Notification
            notificationService.createNotification(
                    user,
                    "Account Deleted",
                    "Your account has been deleted by an admin.",
                    NotificationType.USER
            );

        } catch (DataIntegrityViolationException e) {
            throw BusinessException.invalidInput("Cannot delete user due to existing references");
        }
    }

    // ---------- Helper Methods ----------
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    private void ensureNotAdmin(User user, String action) {
        if (user.getRole() == UserRole.ADMIN) {
            throw BusinessException.operationNotAllowed("Cannot " + action + " an Admin account");
        }
    }
}
