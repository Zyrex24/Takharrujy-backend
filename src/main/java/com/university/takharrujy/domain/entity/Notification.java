package com.university.takharrujy.domain.entity;

import com.university.takharrujy.domain.enums.NotificationType;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Boolean read = false;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Notification() {
        super();
    }

    public Notification(Long universityId) {
        super(universityId);
    }

    // Getters & Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Boolean getRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
