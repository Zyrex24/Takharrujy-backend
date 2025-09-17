package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.TaskStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Task Entity
 * Represents a task within a project
 */
@Entity
@Table(name = "tasks")
public class Task extends BaseEntity {

    @NotBlank(message = "Task title is required")
    @Size(max = 255, message = "Task title cannot exceed 255 characters")
    @Column(name = "title", nullable = false)
    private String title;

    @Size(max = 255, message = "Arabic title cannot exceed 255 characters")
    @Column(name = "title_ar")
    private String titleAr;

    @Size(max = 2000, message = "Task description cannot exceed 2000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Size(max = 2000, message = "Arabic description cannot exceed 2000 characters")
    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status = TaskStatus.TODO;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Column(name = "priority")
    private Integer priority = 1; // 1 = Low, 2 = Medium, 3 = High, 4 = Critical

    @Column(name = "estimated_hours")
    private Integer estimatedHours;

    @Column(name = "actual_hours")
    private Integer actualHours;

    @Column(name = "progress_percentage")
    private Integer progressPercentage = 0;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Size(max = 1000, message = "Arabic notes cannot exceed 1000 characters")
    @Column(name = "notes_ar", columnDefinition = "TEXT")
    private String notesAr;

    @Column(name = "is_milestone")
    private Boolean isMilestone = false;

    @Column(name = "task_order")
    private Integer taskOrder;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_id")
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Task> subtasks = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "task_dependencies",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "depends_on_id")
    )
    private Set<Task> dependencies = new HashSet<>();

    @ManyToMany(mappedBy = "dependencies", fetch = FetchType.LAZY)
    private Set<Task> dependentTasks = new HashSet<>();

    // Constructors
    public Task() {
        super();
    }

    public Task(String title, String description, Project project, Long universityId) {
        super(universityId);
        this.title = title;
        this.description = description;
        this.project = project;
    }

    // Business methods
    public boolean canBeStarted() {
        return dependencies.stream().allMatch(Task::isCompleted);
    }

    public boolean isCompleted() {
        return TaskStatus.COMPLETED.equals(status);
    }

    public boolean isOverdue() {
        return dueDate != null && 
               LocalDate.now().isAfter(dueDate) && 
               !isCompleted();
    }

    public void completeTask() {
        this.status = TaskStatus.COMPLETED;
        this.progressPercentage = 100;
        this.completionDate = LocalDate.now();
    }

    public void startTask() {
        if (this.status == TaskStatus.TODO && canBeStarted()) {
            this.status = TaskStatus.IN_PROGRESS;
            if (this.startDate == null) {
                this.startDate = LocalDate.now();
            }
        }
    }

    public String getPriorityText() {
        return switch (priority) {
            case 1 -> "Low";
            case 2 -> "Medium";
            case 3 -> "High";
            case 4 -> "Critical";
            default -> "Unknown";
        };
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

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Integer estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Integer getActualHours() {
        return actualHours;
    }

    public void setActualHours(Integer actualHours) {
        this.actualHours = actualHours;
    }

    public Integer getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Integer progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getNotesAr() {
        return notesAr;
    }

    public void setNotesAr(String notesAr) {
        this.notesAr = notesAr;
    }

    public Boolean getIsMilestone() {
        return isMilestone;
    }

    public void setIsMilestone(Boolean isMilestone) {
        this.isMilestone = isMilestone;
    }

    public Integer getTaskOrder() {
        return taskOrder;
    }

    public void setTaskOrder(Integer taskOrder) {
        this.taskOrder = taskOrder;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public Task getParentTask() {
        return parentTask;
    }

    public void setParentTask(Task parentTask) {
        this.parentTask = parentTask;
    }

    public Set<Task> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(Set<Task> subtasks) {
        this.subtasks = subtasks;
    }

    public Set<Task> getDependencies() {
        return dependencies;
    }

    public void setDependencies(Set<Task> dependencies) {
        this.dependencies = dependencies;
    }

    public Set<Task> getDependentTasks() {
        return dependentTasks;
    }

    public void setDependentTasks(Set<Task> dependentTasks) {
        this.dependentTasks = dependentTasks;
    }
}