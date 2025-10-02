package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.DeliverableStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "deliverables")
public class Deliverable extends BaseEntity {

    @NotBlank(message = "Deliverable title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private DeliverableStatus status = DeliverableStatus.PENDING;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "submission_notes", length = 2000)
    private String submissionNotes;

    @Column(name = "submission_file_url", length = 500)
    private String submissionFileUrl;

    @Column(name = "supervisor_feedback", length = 2000)
    private String supervisorFeedback;

    @Column(name = "supervisor_feedback_ar", length = 2000)
    private String supervisorFeedbackAr;


    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    // Constructors
    public Deliverable() {
        super();
    }

    public Deliverable(String title, String description, Project project, Long universityId) {
        super(universityId);
        this.title = title;
        this.description = description;
        this.project = project;
    }

    // Business logic
    public boolean isSubmitted() {
        return submittedAt != null;
    }

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public DeliverableStatus getStatus() { return status; }
    public void setStatus(DeliverableStatus status) { this.status = status; }

    public Instant getDueDate() { return dueDate; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }

    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }

    public String getSubmissionNotes() {
        return submissionNotes;
    }

    public void setSubmissionNotes(String submissionNotes) {
        this.submissionNotes = submissionNotes;
    }

    public String getSubmissionFileUrl() {
        return submissionFileUrl;
    }

    public void setSubmissionFileUrl(String submissionFileUrl) {
        this.submissionFileUrl = submissionFileUrl;
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

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }
}
