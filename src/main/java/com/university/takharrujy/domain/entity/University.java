package com.university.takharrujy.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

/**
 * University Entity
 * Represents a university in the multi-tenant system
 */
@Entity
@Table(name = "universities")
public class University extends RootBaseEntity {

    @NotBlank(message = "University name is required")
    @Size(max = 255, message = "University name cannot exceed 255 characters")
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank(message = "University Arabic name is required")
    @Size(max = 255, message = "University Arabic name cannot exceed 255 characters")
    @Column(name = "name_ar", nullable = false)
    private String nameAr;

    @NotBlank(message = "University domain is required")
    @Size(max = 100, message = "University domain cannot exceed 100 characters")
    @Column(name = "domain", nullable = false, unique = true)
    private String domain;

    @Email(message = "Invalid contact email format")
    @Size(max = 255, message = "Contact email cannot exceed 255 characters")
    @Column(name = "contact_email")
    private String contactEmail;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    @Column(name = "phone")
    private String phone;

    @Size(max = 500, message = "Address cannot exceed 500 characters")
    @Column(name = "address")
    private String address;

    @Size(max = 500, message = "Arabic address cannot exceed 500 characters")
    @Column(name = "address_ar")
    private String addressAr;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Size(max = 10, message = "Country code cannot exceed 10 characters")
    @Column(name = "country_code")
    private String countryCode;

    @Size(max = 10, message = "Timezone cannot exceed 50 characters")
    @Column(name = "timezone")
    private String timezone;

    // Relationships
    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "university", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Department> departments = new HashSet<>();

    // Constructors
    public University() {
        super();
    }

    public University(String name, String nameAr, String domain) {
        this.name = name;
        this.nameAr = nameAr;
        this.domain = domain;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressAr() {
        return addressAr;
    }

    public void setAddressAr(String addressAr) {
        this.addressAr = addressAr;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(Set<Department> departments) {
        this.departments = departments;
    }
}