package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.ProjectStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Project Entity
 * Represents a graduation project in the system
 */
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {

    @NotBlank(message = "Project title is required")
    @Size(max = 255, message = "Project title cannot exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 255, message = "Arabic title cannot exceed 255 characters")
    @Column(name = "title_ar")
    private String titleAr;

    @NotBlank(message = "Project description is required")
    @Size(max = 5000, message = "Project description cannot exceed 5000 characters")
    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Size(max = 5000, message = "Arabic description cannot exceed 5000 characters")
    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Convert(converter = com.university.takharrujy.infrastructure.config.ProjectStatusConverter.class)
    @Column(name = "status", nullable = false)
    private ProjectStatus status = ProjectStatus.DRAFT;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Size(max = 100, message = "Project type cannot exceed 100 characters")
    @Column(name = "project_type")
    private String projectType;

    @Size(max = 2000, message = "Objectives cannot exceed 2000 characters")
    @Column(name = "objectives", columnDefinition = "TEXT")
    private String objectives;

    @Size(max = 2000, message = "Arabic objectives cannot exceed 2000 characters")
    @Column(name = "objectives_ar", columnDefinition = "TEXT")
    private String objectivesAr;

    @Size(max = 1000, message = "Technologies cannot exceed 1000 characters")
    @Column(name = "technologies")
    private String technologies;

    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Column(name = "final_grade")
    private BigDecimal finalGrade;

    @Size(max = 2000, message = "Supervisor feedback cannot exceed 2000 characters")
    @Column(name = "supervisor_feedback", columnDefinition = "TEXT")
    private String supervisorFeedback;

    @Size(max = 2000, message = "Arabic supervisor feedback cannot exceed 2000 characters")
    @Column(name = "supervisor_feedback_ar", columnDefinition = "TEXT")
    private String supervisorFeedbackAr;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_leader_id", nullable = false)
    private User teamLeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<ProjectMember> members = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<FileEntity> files = new HashSet<>();

    // Constructors
    public Project() {
        super();
    }

    public Project(String title, String description, User teamLeader, Long universityId) {
        super(universityId);
        this.title = title;
        this.description = description;
        this.teamLeader = teamLeader;
    }

    // Business methods
    public boolean canBeEditedBy(User user) {
        if (!status.isEditable()) {
            return false;
        }
        
        if (user.isAdmin()) {
            return user.getUniversityId().equals(this.getUniversityId());
        }
        
        if (user.isSupervisor()) {
            return this.supervisor != null && this.supervisor.getId().equals(user.getId());
        }
        
        if (user.isStudent()) {
            return this.teamLeader.getId().equals(user.getId()) ||
                   this.members.stream().anyMatch(m -> m.getUser().getId().equals(user.getId()));
        }
        
        return false;
    }

    public int getTeamSize() {
        return members.size() + 1; // +1 for team leader
    }

    public void updateProgress() {
        if (tasks.isEmpty()) {
            this.progressPercentage = 0;
            return;
        }
        
        long completedTasks = tasks.stream()
                .mapToLong(task -> task.isCompleted() ? 1 : 0)
                .sum();
        
        this.progressPercentage = (int) ((completedTasks * 100) / tasks.size());
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleAr() {
        return titleAr;
    }

    public void setTitleAr(String titleAr) {
        this.titleAr = titleAr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescriptionAr() {
        return descriptionAr;
    }

    public void setDescriptionAr(String descriptionAr) {
        this.descriptionAr = descriptionAr;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getObjectives() {
        return objectives;
    }

    public void setObjectives(String objectives) {
        this.objectives = objectives;
    }

    public String getObjectivesAr() {
        return objectivesAr;
    }

    public void setObjectivesAr(String objectivesAr) {
        this.objectivesAr = objectivesAr;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public BigDecimal getFinalGrade() {
        return finalGrade;
    }

    public void setFinalGrade(BigDecimal finalGrade) {
        this.finalGrade = finalGrade;
    }

    public String getSupervisorFeedback() {
        return supervisorFeedback;
    }

    public void setSupervisorFeedback(String supervisorFeedback) {
        this.supervisorFeedback = supervisorFeedback;
    }

    public String getSupervisorFeedbackAr() {
        return supervisorFeedbackAr;
    }

    public void setSupervisorFeedbackAr(String supervisorFeedbackAr) {
        this.supervisorFeedbackAr = supervisorFeedbackAr;
    }

    public User getTeamLeader() {
        return teamLeader;
    }

    public void setTeamLeader(User teamLeader) {
        this.teamLeader = teamLeader;
    }

    public User getSupervisor() {
        return supervisor;
    }

    public void setSupervisor(User supervisor) {
        this.supervisor = supervisor;
    }

    public Set<ProjectMember> getMembers() {
        return members;
    }

    public void setMembers(Set<ProjectMember> members) {
        this.members = members;
    }

    public Set<Task> getTasks() {
        return tasks;
    }

    public void setTasks(Set<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<FileEntity> getFiles() {
        return files;
    }

    public void setFiles(Set<FileEntity> files) {
        this.files = files;
    }
}