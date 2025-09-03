# Takharrujy -- [تَخَرُّجِي]{dir="rtl"}

## Software Requirements Specification

**Version:** 1.0  
**Date:** 07/08/2025  
**Platform URL:** https://takharujy.tech  

**Prepared for**

Graduation Project Platform -- Takharrujy

## Revision History

| Date | Description | Author | Comments |
|------|-------------|---------|----------|
| 10/08/2025 | Version 1.0 | khalid mohamed | Initial Draft |

## Document Approval

The following Software Requirements Specification has been accepted and approved by the following:

| Printed Name | Title | Date |
|--------------|-------|------|
| [Name Placeholder] | Lead Software Engineer | [Date Placeholder] |
| [Name Placeholder] | Lead QA Engineer | [Date Placeholder] |
| [Name Placeholder] | Project Sponsor | [Date Placeholder] |

## Table of Contents

1. [Introduction](#introduction)
   - 1.1 [Purpose](#purpose)
   - 1.2 [Scope](#scope)
   - 1.3 [Definitions, Acronyms, and Abbreviations](#definitions-acronyms-and-abbreviations)
   - 1.4 [References](#references)
2. [Specific Requirements](#specific-requirements)
   - 2.1 [External Interface Requirements](#external-interface-requirements)
3. [Front End Details](#front-end-details)
4. [Technical Requirements](#technical-requirements)
5. [Functional validations](#functional-validations)
6. [Classes / Objects](#classes-objects)
7. [Non-Functional Requirements](#non-functional-requirements)
8. [Inverse Requirements](#inverse-requirements)
9. [Design Constraints](#design-constraints)
10. [Logical Database Requirements](#logical-database-requirements)
11. [Other Requirements](#other-requirements)
12. [Analysis Models](#analysis-models)
13. [Change Management Process](#change-management-process)

# 1. Introduction

The Takharrujy project aims to provide a graduation project management platform to support university students during their final year. The platform is accessible at **https://takharujy.tech** and provides comprehensive project management capabilities.

This release will include core features such as team collaboration, task management, supervisor interaction, and AI-powered academic assistance. Over time, more advanced functionalities will be added to the system.

## 1.1 Purpose

The purpose of this document is to define the requirements for the Takharrujy graduation project management platform. It is intended for use by the developers, testers, and stakeholders involved in the design, development, testing, and deployment of the system.

## 1.2 Scope

This document outlines the functionalities and technical specifications required for building the Takharrujy platform. The system will facilitate project management and team collaboration for final-year university students and their academic supervisors.

The following are in scope:

- Functional testing
- Role-based access for students, supervisors, and admins
- Task management
- File upload and sharing
- AI tools integration for assistance

The following are beyond the current scope:

- Stress and performance testing
- Automation testing

Both a web version and a Flutter-based mobile app will be developed to provide full cross-platform support.

## 1.3 Definitions, Acronyms, and Abbreviations

| Abbreviation | Word |
|-------------|------|
| S | Student |
| SV | Supervisor |
| A | Admin |
| AI | Artificial Intelligence |

## 1.4 References

*Nil*

# 2. Specific Requirements

The system will have 3 roles:

- Admin
- Supervisor
- Student

| Admin | Supervisor | Student |
|--------|------------|---------|
| Manage universities | Review team proposals | Create project group |
| Manage faculties & departments | Approve/reject team requests | Assign team leader |
| View all projects | Communicate with team | Upload deliverables |
| Assign supervisors | Review deliverables | Create & assign tasks |
| Manage platform users | Provide feedback | Mark tasks as complete |
| Access AI tools | Communicate with supervisor |
| | Track project timeline |

## Description of the modules

| Module Name | Applicable Roles | Description |
|-------------|------------------|-------------|
| Project Creation | Student | Students can create a team and initiate a new graduation project |
| Task Management | Student, Supervisor | Students can assign and complete tasks; supervisors can review and track progress |
| Deliverable Upload | Student | Upload documents like proposals, reports, code, presentations |
| AI Assistant | Student | Assist with report generation, Q&A, productivity suggestions |
| Supervisor Assignment | Admin | Assign supervisor to a student group |
| Feedback Module | Supervisor | Supervisor can add comments on each submission |
| Notifications | All | Alerts for submission deadlines, feedback, updates |
| Communication | Student, Supervisor | Internal messaging/chat system for project discussions |
| User Management | Admin | Add, edit, delete users with appropriate roles |

## 2.1 External Interface Requirements

### 2.1.1 User Interfaces

Responsive web interface for desktop and mobile with intuitive navigation, forms, modals, chat box, and dashboards accessible at **https://takharujy.tech**

### 2.1.2 Hardware Interfaces

None

### 2.1.3 Software Interfaces

AI APIs (OpenAI or similar), notification services via Brevo SMTP (smtp-relay.brevo.com), and third-party file storage APIs (e.g., Azure Blob Storage or DigitalOcean Spaces)

### 2.1.4 Communications Interfaces

RESTful API endpoints at **https://api.takharujy.tech/v1** for front-end and mobile integration

# 3. Front End Details

List of key UI fields for each module:

## Project Creation

- Project Title
- Description
- Team Members (Select)
- Supervisor Preference
- Submit, Reset

## Task Management

- Task Title
- Description
- Deadline
- Assigned Member
- Status Dropdown
- Submit, Reset

## Deliverable Upload

- Deliverable Type (Proposal, Report, Code, etc.)
- Upload Field
- Description
- Submit, Reset

## AI Assistant

- Text Prompt Input
- Task Type Dropdown (Q&A, Report Help, Planning, etc.)
- Submit

## Feedback Module

- Deliverable ID
- Comment Box
- Grade (optional)
- Submit

## Login

- Email
- Password
- Submit

## Registration

- Name
- Email
- Role (Student, Supervisor, Admin)
- University
- Password & Confirm Password
- Submit

# 4. Technical Requirements

## Project Validations

**T1** Project title must not be blank  
**T2** Description field must not be blank  
**T3** Team must have at least 2 members  
**T4** File upload type must be supported (PDF, DOCX, PPTX, ZIP)  
**T5** Upload size limit is 100MB  
**T6** Feedback comment must not be blank  
**T7** AI prompt must not be blank  
**T8** Task title must be unique within group  
**T9** Task deadline must be in future  
**T10** Only team leader can submit final deliverables  
**T11** Students can only submit once per deliverable type  
**T12** Uploaded files must be virus-scanned  
**T13** Special characters not allowed in name fields  
**T14** No duplicate project titles within the system  
**T15** Each user must belong to only one team per project  
**T16** Uploaded filenames must not contain special characters  

## Authentication & Authorization

**T17** Email format must be valid  
**T18** Passwords must be at least 8 characters  
**T19** Password must contain special character and number  
**T20** No duplicate emails allowed  
**T21** Only Admin can assign supervisors  
**T22** Supervisor can only view assigned teams  
**T23** Unauthorized role cannot access other modules  
**T24** User-ID must not be blank  
**T25** Password must not be blank  
**T26** Old Password must not be blank  
**T27** New Password must not be blank  
**T28** Confirm Password must not be blank  
**T29** Passwords do not match  
**T30** Enter at least one numeric value in password  
**T31** Enter at least one special character in password  
**T32** Choose a difficult Password  

## Project Submission

**T33** Special characters are not allowed in the Project Title  
**T34** Project Title must not contain numeric digits  

## Team Registration

**T35** Team Name must not be blank  
**T36** Special characters are not allowed in Team Name  
**T37** Duplicate team names are not allowed  

## Task Creation

**T38** Task Title must not be blank  
**T39** Deadline must be selected  
**T40** Special characters are not allowed in Task Title  
**T41** Task Description must not be blank  
**T42** Task priority must be selected from allowed options (Low, Medium, High)  

## Deliverable Upload

**T43** File must be selected before upload  

## AI Assistant Prompt

**T44** Special characters are not allowed in prompt  
**T45** Prompt must not exceed 500 characters  

## Feedback Form

**T46** Feedback rating must be selected  
**T47** Special characters are not allowed in comment  
**T48** Feedback must be at least 10 characters  

## User Registration

**T49** Full Name must not be blank  
**T50** Full Name must not contain special characters  
**T51** First character of name must not be a space  
**T52** Email must not be blank  
**T53** Duplicate emails are not allowed  
**T54** Confirm Password must not be blank  
**T55** Password must not contain spaces  
**T56** Only university domain emails are allowed (e.g. @student.uni.edu)  

## Login

**T57** Invalid email or password must trigger error message  
**T58** Captcha must be completed after 3 failed attempts  

## Password Reset

**T59** New password must differ from old password  
**T60** New Password must meet complexity rules (number, special char)  
**T61** Password reset link must expire after 30 minutes  

## Supervisor Assignment

**T62** Supervisor must not exceed allowed number of assigned teams  

## Notifications & Messaging

**T63** Message field must not be blank  
**T64** Message must not exceed 1000 characters  
**T65** First character must not be a space  
**T66** No special characters allowed in message subject  

# 5. Functional validations

## Project Creation

**F1** Error is shown if required fields are missing  
**F2** User cannot create another team if already part of an existing one  

## Task Management

**F3** Only assigned member can mark task as complete  
**F4** Warning is shown if task deadline is in the past  

## Deliverable Upload

**F5** Uploaded file must be within size and format limits  
**F6** Confirmation message is shown after successful upload  

## Supervisor Feedback

**F7** Supervisor can only comment on projects assigned to them  
**F8** Blank feedback is not accepted  

## AI Assistant

**F9** No response is returned if input is blank or irrelevant  
**F10** System displays a warning on inappropriate prompts  

## Authentication

**F11** Error is shown when password is incorrect  
**F12** Login fails after 5 incorrect attempts  

## Role Restrictions

**F13** Students cannot access the admin panel  
**F14** Supervisors cannot remove team members  
**F15** Only admins can create faculties  

## Communication

**F16** Students can only message their assigned supervisor  
**F17** Supervisors can only reply to their assigned teams  

# 6. Classes / Objects

(Will be defined in implementation phase)

## 6.1 Attributes

## 6.2 Functions

# 7. Non-Functional Requirements

- System should support up to 10,000 concurrent users
- Average page load time < 3 seconds
- 99.9% uptime required
- Mobile-friendly UI
- Secure authentication with JWT or OAuth
- Data encrypted in transit and at rest
- Platform hosted at https://takharujy.tech
- API endpoints available at https://api.takharujy.tech/v1

# 8. Inverse Requirements

Nil.

# 9. Design Constraints

The system must be intuitive to use for students with no prior experience using project management tools. User interface should prioritize clarity, simplicity, and responsiveness. The platform must be accessible via https://takharujy.tech with full mobile responsiveness.

# 10. Logical Database Requirements

(To be implemented in PostgreSQL. Schema includes Users, Projects, Tasks, Deliverables, Feedback, Notifications, Messages.)

# 11. Other Requirements

- Multilingual support (Arabic, English)
- Email notifications via Brevo SMTP service (smtp-relay.brevo.com)
- From address: donotreply@takharujy.tech
- Dashboard accessible at: https://takharujy.tech/dashboard

# 12. Analysis Models

Nil

# 13. Change Management Process

Any proposed changes to the SRS from the development, testing, or client side will be submitted to the project sponsor. Each change must be approved by the QA lead, development lead, and sponsor before being incorporated.

Once approved, changes will be versioned and shared with all stakeholders.

## A. Appendices

*Nil*