package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity
 * Represents a user in the system (Student, Supervisor, or Admin)
 */
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = 255, message = "Password hash cannot exceed 255 characters")
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Size(max = 100, message = "Arabic first name cannot exceed 100 characters")
    @Column(name = "first_name_ar")
    private String firstNameAr;

    @Size(max = 100, message = "Arabic last name cannot exceed 100 characters")
    @Column(name = "last_name_ar")
    private String lastNameAr;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Size(max = 20, message = "Student ID cannot exceed 20 characters")
    @Column(name = "student_id", unique = true)
    private String studentId;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone")
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Size(max = 255, message = "Profile picture URL cannot exceed 255 characters")
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Size(max = 1000, message = "Arabic bio cannot exceed 1000 characters")
    @Column(name = "bio_ar", columnDefinition = "TEXT")
    private String bioAr;

    @Size(max = 10, message = "Preferred language cannot exceed 10 characters")
    @Column(name = "preferred_language")
    private String preferredLanguage = "ar";

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false, insertable = false, updatable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @OneToMany(mappedBy = "teamLeader", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Project> leaderProjects = new HashSet<>();

    @OneToMany(mappedBy = "supervisor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Project> supervisedProjects = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ProjectMember> projectMemberships = new HashSet<>();

    // Constructors
    public User() {
        super();
    }

    public User(String email, String firstName, String lastName, UserRole role, Long universityId) {
        super(universityId);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
    }

    // Business methods
    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFullNameAr() {
        if (firstNameAr != null && lastNameAr != null) {
            return firstNameAr + " " + lastNameAr;
        }
        return getFullName();
    }

    public boolean isStudent() {
        return UserRole.STUDENT.equals(role);
    }

    public boolean isSupervisor() {
        return UserRole.SUPERVISOR.equals(role);
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstNameAr() {
        return firstNameAr;
    }

    public void setFirstNameAr(String firstNameAr) {
        this.firstNameAr = firstNameAr;
    }

    public String getLastNameAr() {
        return lastNameAr;
    }

    public void setLastNameAr(String lastNameAr) {
        this.lastNameAr = lastNameAr;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsEmailVerified() {
        return isEmailVerified;
    }

    public void setIsEmailVerified(Boolean isEmailVerified) {
        this.isEmailVerified = isEmailVerified;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getBioAr() {
        return bioAr;
    }

    public void setBioAr(String bioAr) {
        this.bioAr = bioAr;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public University getUniversity() {
        return university;
    }

    public void setUniversity(University university) {
        this.university = university;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Set<Project> getLeaderProjects() {
        return leaderProjects;
    }

    public void setLeaderProjects(Set<Project> leaderProjects) {
        this.leaderProjects = leaderProjects;
    }

    public Set<Project> getSupervisedProjects() {
        return supervisedProjects;
    }

    public void setSupervisedProjects(Set<Project> supervisedProjects) {
        this.supervisedProjects = supervisedProjects;
    }

    public Set<ProjectMember> getProjectMemberships() {
        return projectMemberships;
    }

    public void setProjectMemberships(Set<ProjectMember> projectMemberships) {
        this.projectMemberships = projectMemberships;
    }
}