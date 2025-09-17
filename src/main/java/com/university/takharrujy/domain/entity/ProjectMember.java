package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.MemberRole;
import com.university.takharrujy.domain.enums.MemberStatus;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Project Member Entity
 * Represents the relationship between users and projects
 */
@Entity
@Table(name = "project_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}))
public class ProjectMember extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role = MemberRole.MEMBER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status = MemberStatus.PENDING;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "invitation_sent_at")
    private Instant invitationSentAt;

    @Column(name = "invitation_accepted_at")
    private Instant invitationAcceptedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by_id")
    private User invitedBy;

    // Constructors
    public ProjectMember() {
        super();
    }

    public ProjectMember(Project project, User user, MemberRole role, User invitedBy) {
        super(project.getUniversityId());
        this.project = project;
        this.user = user;
        this.role = role;
        this.invitedBy = invitedBy;
        this.invitationSentAt = Instant.now();
    }

    // Business methods
    public void acceptInvitation() {
        this.status = MemberStatus.ACTIVE;
        this.invitationAcceptedAt = Instant.now();
        this.joinedAt = Instant.now();
    }

    public void rejectInvitation() {
        this.status = MemberStatus.REJECTED;
    }

    public void removeMember() {
        this.status = MemberStatus.REMOVED;
    }

    public boolean isActive() {
        return MemberStatus.ACTIVE.equals(this.status);
    }

    public boolean isPending() {
        return MemberStatus.PENDING.equals(this.status);
    }

    // Getters and Setters
    public MemberRole getRole() {
        return role;
    }

    public void setRole(MemberRole role) {
        this.role = role;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getInvitationSentAt() {
        return invitationSentAt;
    }

    public void setInvitationSentAt(Instant invitationSentAt) {
        this.invitationSentAt = invitationSentAt;
    }

    public Instant getInvitationAcceptedAt() {
        return invitationAcceptedAt;
    }

    public void setInvitationAcceptedAt(Instant invitationAcceptedAt) {
        this.invitationAcceptedAt = invitationAcceptedAt;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(User invitedBy) {
        this.invitedBy = invitedBy;
    }
}