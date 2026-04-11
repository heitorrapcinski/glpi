# Requirements Document

## Introduction

This document defines the functional and non-functional requirements for rebuilding the GLPI ITSM backend as a modern microservices architecture. The source of truth for domain behavior is the GLPI v12.0.0 legacy codebase located in the `.legacy` folder (read-only reference).

The target system is a production-ready, cloud-native backend exposing REST APIs for all ITSM operations. It is built with Java 21, Spring Boot 3.x, MongoDB, and Apache Kafka, following Hexagonal Architecture (Ports & Adapters) and Domain-Driven Design (DDD) principles. The frontend is out of scope; this spec covers backend services only.

The MVP covers nine bounded contexts: Identity & Access Management, Ticket Management, Problem Management, Change Management, Asset/CMDB, SLA/OLA Management, Notification, Knowledge Base, and API Gateway.

---

## Glossary

- **System**: The entire GLPI Microservices Backend platform.
- **Identity_Service**: The microservice responsible for users, groups, entities, profiles, and authentication.
- **Ticket_Service**: The microservice responsible for incidents and service requests (ITIL tickets).
- **Problem_Service**: The microservice responsible for ITIL problems.
- **Change_Service**: The microservice responsible for ITIL changes.
- **Asset_Service**: The microservice responsible for CMDB assets (computers, network equipment, software, licenses).
- **SLA_Service**: The microservice responsible for SLA/OLA definitions, calendars, and escalation levels.
- **Notification_Service**: The microservice responsible for event-driven notifications via Kafka and email/webhook delivery.
- **Knowledge_Service**: The microservice responsible for knowledge base articles, categories, and FAQ.
- **API_Gateway**: The edge service responsible for routing, authentication enforcement, and rate limiting.
- **User**: A human actor who interacts with the system via the REST API.
- **Entity**: An organizational unit in the multi-tenant hierarchy (equivalent to a tenant or department).
- **Profile**: A named role that grants a set of permissions to a user within an entity.
- **Group**: A collection of users that can be assigned as actors on ITIL objects.
- **Ticket**: An ITIL object representing an Incident (type=1) or a Service Request (type=2).
- **Problem**: An ITIL object representing the root cause of one or more incidents.
- **Change**: An ITIL object representing a planned modification to IT services.
- **Asset**: A Configuration Item (CI) managed in the CMDB (e.g., Computer, NetworkEquipment).
- **SLA**: Service Level Agreement — an external commitment on response/resolution time.
- **OLA**: Operational Level Agreement — an internal commitment supporting SLA delivery.
- **TTO**: Time To Own — the deadline by which a ticket must be assigned.
- **TTR**: Time To Resolve — the deadline by which a ticket must be resolved.
- **Calendar**: A business-hours definition used for SLA/OLA time computation.
- **Domain_Event**: An immutable record of something that happened within a bounded context, published to Kafka.
- **JWT**: JSON Web Token used for stateless authentication.
- **RBAC**: Role-Based Access Control.
- **EARS**: Easy Approach to Requirements Syntax — the pattern used for all acceptance criteria.
- **Aggregate**: A DDD cluster of domain objects treated as a single unit for data changes.
- **Repository**: A DDD pattern providing collection-like access to aggregates via MongoDB.
- **Port**: A Hexagonal Architecture interface defining how the application interacts with the outside world.
- **Adapter**: A Hexagonal Architecture implementation of a Port (e.g., REST controller, Kafka producer).
- **Priority_Matrix**: The ITIL matrix that derives ticket priority from impact and urgency values (1=Very Low to 6=Very High).

---

## Requirements

### Requirement 1: Identity & Access Management — User Lifecycle

**User Story:** As a system administrator, I want to manage user accounts with authentication credentials, profile assignments, and entity memberships, so that users can securely access the system with appropriate permissions.

#### Acceptance Criteria

1. THE Identity_Service SHALL store each user with the following mandatory fields: unique username, hashed password (bcrypt), email address, active status, and creation timestamp.
2. WHEN a user creation request is received with a duplicate username within the same authentication type and authentication source, THE Identity_Service SHALL reject the request with HTTP 409 and a descriptive error message.
3. WHEN a user creation request is received with a password that does not meet the configured complexity policy, THE Identity_Service SHALL reject the request with HTTP 422 and enumerate all policy violations.
4. THE Identity_Service SHALL support the following authentication types: local database (DB_GLPI=1), LDAP (LDAP=2), and external OAuth2 (OAUTH2=4).
5. WHEN a user is deactivated (is_active=false), THE Identity_Service SHALL reject all subsequent authentication attempts for that user with HTTP 401.
6. THE Identity_Service SHALL generate a unique personal API token per user upon request, stored encrypted at rest.
7. WHEN a personal API token is presented in the Authorization header, THE Identity_Service SHALL authenticate the request without requiring a password.
8. THE Identity_Service SHALL support Two-Factor Authentication (2FA) using TOTP (RFC 6238) when enabled on the user account.
9. WHEN 2FA is enforced at the profile level (2fa_enforced=true), THE Identity_Service SHALL require TOTP verification on every login for users assigned to that profile.
10. THE Identity_Service SHALL maintain a password history of the last 5 passwords and SHALL reject reuse of any password in that history.
11. WHEN a user is purged, THE Identity_Service SHALL publish a UserPurged domain event to Kafka containing the user ID and timestamp.
12. THE Identity_Service SHALL support user impersonation by administrators with the IMPERSONATE right, recording the impersonating user ID in all audit logs during the session.

---

### Requirement 2: Identity & Access Management — Entity Hierarchy

**User Story:** As a system administrator, I want to organize the system into a hierarchical tree of entities (organizational units), so that data and permissions can be scoped per department or customer.

#### Acceptance Criteria

1. THE Identity_Service SHALL represent entities as a recursive tree structure where each entity has exactly one parent entity, except the root entity (id=0) which has no parent.
2. THE Identity_Service SHALL enforce that entity names are unique within the same parent entity.
3. WHEN an entity is deleted, THE Identity_Service SHALL reject the deletion if the entity has child entities, returning HTTP 409.
4. THE Identity_Service SHALL propagate configuration values (calendars, SLA strategies, notification settings) from parent entities to child entities using an inheritance strategy (CONFIG_PARENT=-2).
5. WHEN a user is assigned to an entity with a profile, THE Identity_Service SHALL grant that user access to all descendant entities when the assignment is marked as recursive.
6. THE Identity_Service SHALL support entity-level configuration overrides for: default ticket type, auto-assignment mode, auto-close delay, satisfaction survey settings, and calendar assignment.
7. WHEN an entity configuration field is set to CONFIG_PARENT (-2), THE Identity_Service SHALL resolve the effective value by traversing the entity tree upward until a non-inherited value is found.

---

### Requirement 3: Identity & Access Management — Profiles and RBAC

**User Story:** As a system administrator, I want to define profiles with fine-grained permission sets, so that users are granted only the rights they need to perform their roles.

#### Acceptance Criteria

1. THE Identity_Service SHALL support two profile interfaces: "central" (full ITSM interface) and "helpdesk" (self-service interface).
2. THE Identity_Service SHALL represent permissions as bitfield values per right name, supporting the standard CRUD bits: READ=1, UPDATE=2, CREATE=4, DELETE=8, PURGE=16.
3. THE Identity_Service SHALL support the following ticket-specific rights: READMY=1, READALL=1024, ASSIGN=8192, STEAL=16384, OWN=32768, CHANGEPRIORITY=65536, READNEWTICKET=262144.
4. WHEN a JWT token is validated, THE API_Gateway SHALL include the user's active profile rights in the security context passed to downstream services.
5. THE Identity_Service SHALL prevent deletion of the last profile that holds the UPDATE right on the "profile" resource.
6. WHEN a profile is assigned to a user in an entity, THE Identity_Service SHALL publish a ProfileAssigned domain event to Kafka.
7. THE Identity_Service SHALL support a default profile that is automatically assigned to newly created users when no explicit profile is specified.

---

### Requirement 4: Identity & Access Management — Authentication and JWT

**User Story:** As an API client, I want to authenticate using credentials or tokens and receive a JWT for subsequent requests, so that I can securely call all protected API endpoints.

#### Acceptance Criteria

1. WHEN valid credentials (username + password) are submitted to POST /auth/login, THE Identity_Service SHALL return a signed JWT access token (expiry: 1 hour) and a refresh token (expiry: 7 days).
2. WHEN an expired JWT is presented, THE API_Gateway SHALL return HTTP 401 with error code TOKEN_EXPIRED.
3. WHEN a valid refresh token is submitted to POST /auth/refresh, THE Identity_Service SHALL return a new JWT access token and rotate the refresh token.
4. WHEN a refresh token is used more than once (replay attack), THE Identity_Service SHALL invalidate the entire refresh token family and return HTTP 401.
5. THE Identity_Service SHALL sign JWTs using RS256 (RSA + SHA-256) with a configurable key pair.
6. WHEN a user logs out via POST /auth/logout, THE Identity_Service SHALL add the JWT to a blocklist (stored in a fast cache) for the remainder of its validity period.
7. THE JWT payload SHALL contain: sub (user ID), entity_id (active entity), profile_id (active profile), rights (serialized permission map), iat, exp.
8. WHEN an App-Token header is present, THE API_Gateway SHALL validate it against the configured API client registry before processing the request.

---

### Requirement 5: Ticket Management — Ticket Lifecycle

**User Story:** As a support technician, I want to create, update, and track tickets for incidents and service requests, so that I can manage the full ITIL service operation lifecycle.

#### Acceptance Criteria

1. THE Ticket_Service SHALL support two ticket types: INCIDENT (type=1) and SERVICE_REQUEST (type=2).
2. THE Ticket_Service SHALL enforce the following status values: INCOMING=1, ASSIGNED=2, PLANNED=3, WAITING=4, SOLVED=5, CLOSED=6.
3. WHEN a ticket is created, THE Ticket_Service SHALL assign status INCOMING and record the creation timestamp, the requesting user, and the entity.
4. WHEN a ticket is assigned to a user or group, THE Ticket_Service SHALL transition the status from INCOMING to ASSIGNED and record the assignment timestamp.
5. WHEN a ticket status transitions to SOLVED, THE Ticket_Service SHALL record the solve date and publish a TicketSolved domain event to Kafka.
6. WHEN a ticket status transitions to CLOSED, THE Ticket_Service SHALL record the close date and publish a TicketClosed domain event to Kafka.
7. IF a ticket is in CLOSED or SOLVED status and a followup is added by the requester, THEN THE Ticket_Service SHALL reopen the ticket to INCOMING status and publish a TicketReopened domain event.
8. THE Ticket_Service SHALL compute ticket priority using the Priority_Matrix: priority = matrix[impact][urgency], where impact and urgency each range from 1 (very low) to 5 (very high) and priority ranges from 1 (very low) to 6 (major).
9. THE Ticket_Service SHALL enforce that priority can only be changed by users with the CHANGEPRIORITY right.
10. WHEN a ticket is created or updated, THE Ticket_Service SHALL publish a TicketCreated or TicketUpdated domain event to Kafka containing the full ticket snapshot.
11. THE Ticket_Service SHALL support soft deletion (is_deleted=true) and SHALL exclude soft-deleted tickets from all default search results.
12. WHEN a ticket is deleted, THE Ticket_Service SHALL publish a TicketDeleted domain event to Kafka.

---

### Requirement 6: Ticket Management — Actors

**User Story:** As a support technician, I want to assign requesters, assigned technicians, observers, and suppliers to a ticket, so that all stakeholders are tracked and notified appropriately.

#### Acceptance Criteria

1. THE Ticket_Service SHALL support four actor types per ticket: REQUESTER (type=1), ASSIGNED (type=2), OBSERVER (type=3), and SUPPLIER (type=4).
2. THE Ticket_Service SHALL allow multiple actors of each type on a single ticket (e.g., multiple assigned technicians).
3. WHEN an actor is added to a ticket, THE Ticket_Service SHALL record whether the actor has notifications enabled (use_notification flag).
4. THE Ticket_Service SHALL allow actors to be users, groups, or suppliers.
5. WHEN a user with only the OWN right attempts to assign a ticket to themselves and no assigned user exists, THE Ticket_Service SHALL permit the self-assignment.
6. WHEN a user with the STEAL right attempts to assign a ticket already assigned to another user, THE Ticket_Service SHALL permit the reassignment.
7. WHEN actors are modified on a ticket, THE Ticket_Service SHALL publish a TicketActorsUpdated domain event to Kafka.

---

### Requirement 7: Ticket Management — Followups, Tasks, and Solutions

**User Story:** As a support technician, I want to add followups, tasks, and solutions to a ticket, so that I can document the resolution process and communicate with the requester.

#### Acceptance Criteria

1. THE Ticket_Service SHALL support ITILFollowup entries on tickets, each containing: content (rich text), author, timestamp, is_private flag, and source (email, phone, web, etc.).
2. THE Ticket_Service SHALL support ITILTask entries on tickets, each containing: content, assigned user, planned start/end dates, actual duration, status (TODO=1, DONE=2), and is_private flag.
3. THE Ticket_Service SHALL support exactly one active ITILSolution per ticket, containing: content, solution type, author, and timestamp.
4. WHEN a solution is added to a ticket, THE Ticket_Service SHALL transition the ticket status to SOLVED.
5. WHEN a solution is rejected by the requester, THE Ticket_Service SHALL reopen the ticket to ASSIGNED status and record the rejection reason.
6. WHEN a followup is added to a ticket, THE Ticket_Service SHALL publish a TicketFollowupAdded domain event to Kafka.
7. WHEN a task is completed (status=DONE), THE Ticket_Service SHALL publish a TicketTaskCompleted domain event to Kafka.

---

### Requirement 8: Ticket Management — Validation Workflow

**User Story:** As a manager, I want to approve or reject ticket solutions and service request validations, so that quality control is enforced before tickets are closed.

#### Acceptance Criteria

1. THE Ticket_Service SHALL support TicketValidation records linked to a ticket, each containing: validator (user or group), status (WAITING=1, ACCEPTED=2, REFUSED=3, NONE=4), and comment.
2. WHEN a validation is requested, THE Ticket_Service SHALL set the ticket status to WAITING and publish a TicketValidationRequested domain event to Kafka.
3. WHEN all required validations are approved, THE Ticket_Service SHALL allow the ticket to proceed to SOLVED status.
4. WHEN a validation is refused, THE Ticket_Service SHALL reopen the ticket and publish a TicketValidationRefused domain event to Kafka.
5. THE Ticket_Service SHALL support global validation (any one validator can approve) and sequential validation (all validators must approve in order).

---

### Requirement 9: Ticket Management — SLA Integration

**User Story:** As a service manager, I want SLA deadlines to be automatically computed and tracked on tickets, so that I can monitor compliance and trigger escalations.

#### Acceptance Criteria

1. WHEN a ticket is created and an SLA of type TTO is configured for the entity, THE Ticket_Service SHALL compute the time_to_own deadline using the SLA duration and the entity's calendar.
2. WHEN a ticket is created and an SLA of type TTR is configured for the entity, THE Ticket_Service SHALL compute the time_to_resolve deadline using the SLA duration and the entity's calendar.
3. WHEN a ticket enters WAITING status, THE Ticket_Service SHALL pause the SLA timer and record the waiting start time.
4. WHEN a ticket exits WAITING status, THE Ticket_Service SHALL resume the SLA timer, adding the waiting duration to the sla_waiting_duration field.
5. WHEN a ticket is assigned (TTO met), THE Ticket_Service SHALL record the takeintoaccount_delay_stat in seconds.
6. WHEN a ticket is solved (TTR met), THE Ticket_Service SHALL record the solve_delay_stat in seconds.
7. WHEN an SLA is reassigned to a ticket, THE Ticket_Service SHALL recalculate all SLA deadlines from the current date.
8. THE Ticket_Service SHALL support OLA (internal) deadlines in parallel with SLA deadlines, using the internal_time_to_own and internal_time_to_resolve fields.

---

### Requirement 10: Problem Management — Problem Lifecycle

**User Story:** As a problem manager, I want to create and manage ITIL problems linked to incidents and assets, so that I can identify root causes and prevent recurrence.

#### Acceptance Criteria

1. THE Problem_Service SHALL support the following status values: INCOMING=1, ACCEPTED=7, ASSIGNED=2, PLANNED=3, WAITING=4, SOLVED=5, OBSERVED=8, CLOSED=6.
2. WHEN a problem is created, THE Problem_Service SHALL assign status INCOMING and publish a ProblemCreated domain event to Kafka.
3. THE Problem_Service SHALL support linking problems to tickets (Problem_Ticket relationship) with a many-to-many cardinality.
4. WHEN a ticket is linked to a problem, THE Problem_Service SHALL publish a ProblemTicketLinked domain event to Kafka.
5. THE Problem_Service SHALL support the following analysis fields: impactcontent (rich text), causecontent (root cause analysis), and symptomcontent (symptom description).
6. WHEN a problem is solved, THE Problem_Service SHALL record the solve date and publish a ProblemSolved domain event to Kafka.
7. THE Problem_Service SHALL support linking problems to assets (Item_Problem relationship) to track affected configuration items.
8. THE Problem_Service SHALL support actor types: REQUESTER, ASSIGNED, OBSERVER, and SUPPLIER, following the same rules as Ticket actors.
9. WHEN a problem is closed, THE Problem_Service SHALL publish a ProblemClosed domain event to Kafka.

---

### Requirement 11: Change Management — Change Lifecycle

**User Story:** As a change manager, I want to manage ITIL changes with extended status workflows, validation steps, and planning documents, so that changes are controlled and traceable.

#### Acceptance Criteria

1. THE Change_Service SHALL support the following status values: INCOMING=1, EVALUATION=9, APPROVAL=10, ACCEPTED=7, WAITING=4, TEST=11, QUALIFICATION=12, SOLVED=5, OBSERVED=8, CLOSED=6, CANCELED=14, REFUSED=13.
2. WHEN a change is created, THE Change_Service SHALL assign status INCOMING and publish a ChangeCreated domain event to Kafka.
3. THE Change_Service SHALL support the following planning document fields: impactcontent, controlistcontent (control list), rolloutplancontent (deployment plan), backoutplancontent (backup plan), and checklistcontent.
4. THE Change_Service SHALL support ChangeValidation records with the same structure as TicketValidation.
5. WHEN a change validation is approved, THE Change_Service SHALL publish a ChangeValidationApproved domain event to Kafka.
6. THE Change_Service SHALL support linking changes to tickets (Change_Ticket), problems (Change_Problem), and assets (Change_Item).
7. WHEN a change is linked to a ticket, THE Change_Service SHALL publish a ChangeTicketLinked domain event to Kafka.
8. THE Change_Service SHALL support actor types: REQUESTER, ASSIGNED, OBSERVER, and SUPPLIER.
9. WHEN a change is closed, THE Change_Service SHALL publish a ChangeClosed domain event to Kafka.
10. THE Change_Service SHALL support satisfaction surveys on closed changes, following the same survey model as tickets.

---

### Requirement 12: Asset/CMDB — Asset Lifecycle

**User Story:** As an IT asset manager, I want to register and track configuration items (computers, network equipment, software, licenses) in the CMDB, so that I have a complete inventory of IT assets and their relationships.

#### Acceptance Criteria

1. THE Asset_Service SHALL support the following asset types as first-class entities: Computer, NetworkEquipment, Monitor, Printer, Phone, Peripheral, Software, SoftwareLicense.
2. THE Asset_Service SHALL store each asset with the following mandatory fields: name, entity_id, serial number (optional), inventory number (optional), state, location, assigned user, assigned group, and manufacturer.
3. THE Asset_Service SHALL support asset states with a configurable lifecycle (e.g., In Stock, In Use, Maintenance, Retired, Disposed).
4. THE Asset_Service SHALL support hierarchical locations (Location tree) with a completename computed from the full path.
5. WHEN an asset is created, THE Asset_Service SHALL publish an AssetCreated domain event to Kafka.
6. WHEN an asset is updated, THE Asset_Service SHALL publish an AssetUpdated domain event to Kafka.
7. WHEN an asset is deleted (soft delete), THE Asset_Service SHALL publish an AssetDeleted domain event to Kafka.
8. THE Asset_Service SHALL support linking assets to tickets, problems, and changes via item-link relationships.
9. THE Asset_Service SHALL support Computer-specific sub-entities: operating system installation (Item_OperatingSystem), hardware components (Item_Devices: CPU, RAM, HDD, GPU), disk partitions (Item_Disk), software installations (Item_SoftwareVersion), network ports (NetworkPort), and virtual machines (ItemVirtualMachine).
10. THE Asset_Service SHALL support NetworkPort entries per asset, each containing: port name, MAC address, IP address, VLAN, and connection type.
11. THE Asset_Service SHALL support financial information (Infocom) per asset: purchase date, purchase price, warranty expiry, order number, delivery date, and depreciation data.
12. THE Asset_Service SHALL support contract associations (Contract_Item) linking assets to maintenance or warranty contracts.

---

### Requirement 13: Asset/CMDB — Software and License Management

**User Story:** As an IT asset manager, I want to track software installations and license compliance, so that I can ensure the organization is not over- or under-licensed.

#### Acceptance Criteria

1. THE Asset_Service SHALL support Software entities with: name, manufacturer, category, and version list.
2. THE Asset_Service SHALL support SoftwareLicense entities with: name, software reference, license type, serial number, number of seats, expiry date, and entity.
3. THE Asset_Service SHALL track software installations (Item_SoftwareVersion) linking a software version to a specific asset.
4. WHEN the number of active software installations for a license exceeds the licensed seat count, THE Asset_Service SHALL publish a LicenseOverused domain event to Kafka.
5. THE Asset_Service SHALL support querying the license compliance status: total seats, used seats, and remaining seats for any given software license.

---

### Requirement 14: SLA/OLA Management — SLA and OLA Definitions

**User Story:** As a service manager, I want to define SLAs and OLAs with duration, type, and escalation levels, so that response and resolution time commitments are enforced on tickets.

#### Acceptance Criteria

1. THE SLA_Service SHALL support SLA entities with: name, entity, type (TTO=1 or TTR=2), duration in seconds, and associated calendar.
2. THE SLA_Service SHALL support OLA entities with the same structure as SLA but scoped to internal teams.
3. THE SLA_Service SHALL support SlaLevel (escalation level) entities linked to an SLA, each containing: name, execution delay in seconds, and a list of actions (e.g., send notification, reassign, change priority).
4. WHEN an SLA is assigned to a ticket, THE SLA_Service SHALL compute the deadline date by adding the SLA duration (in business seconds, respecting the calendar) to the ticket creation date.
5. THE SLA_Service SHALL support Calendar entities with: name, entity, and a list of CalendarSegment entries (day of week, start time, end time).
6. THE SLA_Service SHALL support Holiday entries linked to a calendar, each containing: name, date, and is_recurring flag.
7. WHEN computing an SLA deadline, THE SLA_Service SHALL exclude non-business hours and holidays defined in the associated calendar.
8. THE SLA_Service SHALL expose a computeDeadline(startDate, durationSeconds, calendarId) operation that returns the deadline date accounting for business hours.
9. FOR ALL valid (startDate, durationSeconds, calendarId) inputs, parsing the computed deadline and re-computing the remaining duration SHALL yield a value within 1 second of the original duration (round-trip property).

---

### Requirement 15: SLA/OLA Management — Escalation

**User Story:** As a service manager, I want SLA escalation levels to trigger automated actions when deadlines approach, so that at-risk tickets are handled before SLA breach.

#### Acceptance Criteria

1. THE SLA_Service SHALL evaluate escalation levels for all active tickets on a configurable schedule (default: every 5 minutes).
2. WHEN a ticket's SLA deadline is within the escalation level's execution delay, THE SLA_Service SHALL publish an SlaEscalationTriggered domain event to Kafka containing the ticket ID, SLA ID, level ID, and triggered actions.
3. WHEN an SLA escalation action is "send notification", THE Notification_Service SHALL consume the SlaEscalationTriggered event and dispatch the configured notification.
4. WHEN an SLA escalation action is "reassign", THE Ticket_Service SHALL consume the SlaEscalationTriggered event and update the ticket's assigned group or user.
5. WHEN an SLA escalation action is "change priority", THE Ticket_Service SHALL consume the SlaEscalationTriggered event and update the ticket priority.
6. THE SLA_Service SHALL record each escalation execution with timestamp and outcome to prevent duplicate triggering.

---

### Requirement 16: Notification Service — Event-Driven Notifications

**User Story:** As a system administrator, I want notifications to be sent automatically when ITIL events occur, so that all stakeholders are informed in real time.

#### Acceptance Criteria

1. THE Notification_Service SHALL consume domain events from Kafka and dispatch notifications to configured targets.
2. THE Notification_Service SHALL support the following notification events: ticket.created, ticket.updated, ticket.solved, ticket.closed, ticket.deleted, ticket.validation.requested, ticket.validation.approved, ticket.validation.refused, problem.created, problem.solved, change.created, change.validation.approved, sla.escalation.triggered.
3. THE Notification_Service SHALL support the following notification channels: email (SMTP) and webhook (HTTP POST).
4. THE Notification_Service SHALL support NotificationTemplate entities with: event name, subject template, body template (HTML), and language.
5. THE Notification_Service SHALL resolve notification targets from the ticket/problem/change actors: requester, assigned user, assigned group members, observers, and validators.
6. WHEN a notification fails to deliver (SMTP error, webhook timeout), THE Notification_Service SHALL retry up to 3 times with exponential backoff (1s, 4s, 16s) before marking the notification as failed.
7. THE Notification_Service SHALL store all outbound notifications in a QueuedNotification collection with status (PENDING, SENT, FAILED) and delivery timestamp.
8. WHEN a notification is successfully delivered, THE Notification_Service SHALL update the QueuedNotification status to SENT and record the delivery timestamp.
9. THE Notification_Service SHALL support per-entity notification configuration: sender email, reply-to email, mailing signature, and notification delay.
10. WHEN a user has notifications disabled (use_notification=false) for a specific actor role, THE Notification_Service SHALL skip that user when resolving notification targets.

---

### Requirement 17: Knowledge Base — Article Management

**User Story:** As a knowledge manager, I want to create and manage knowledge base articles with visibility rules and FAQ flags, so that users can find solutions to common problems.

#### Acceptance Criteria

1. THE Knowledge_Service SHALL support KnowbaseItem entities with: title, answer (rich text), author, creation date, last update date, is_faq flag, view counter, begin_date (visibility start), and end_date (visibility end).
2. THE Knowledge_Service SHALL support KnowbaseItemCategory entities in a hierarchical tree structure.
3. THE Knowledge_Service SHALL support visibility rules per article: visible to specific users, groups, profiles, or entities (with optional recursive entity visibility).
4. WHEN an anonymous user requests an article, THE Knowledge_Service SHALL return only articles where is_faq=true and the entity visibility includes the root entity recursively.
5. WHEN a helpdesk user requests articles, THE Knowledge_Service SHALL return only articles where is_faq=true and the user's entity/group/profile matches the visibility rules.
6. WHEN a central user requests articles, THE Knowledge_Service SHALL return all articles matching the user's entity/group/profile visibility rules.
7. THE Knowledge_Service SHALL support KnowbaseItem_Revision entries to maintain a full version history of article edits.
8. THE Knowledge_Service SHALL support KnowbaseItem_Comment entries allowing users to comment on articles.
9. WHEN an article is viewed, THE Knowledge_Service SHALL increment the view counter atomically.
10. WHEN an article is created or updated, THE Knowledge_Service SHALL publish a KnowledgeArticleCreated or KnowledgeArticleUpdated domain event to Kafka.
11. THE Knowledge_Service SHALL support linking articles to tickets, problems, and changes (KnowbaseItem_Item relationship).
12. THE Knowledge_Service SHALL support full-text search on article title and answer content.

---

### Requirement 18: API Gateway — Routing and Security

**User Story:** As an API client, I want a single entry point that routes requests to the correct microservice, enforces authentication, and applies rate limiting, so that the backend is secure and manageable.

#### Acceptance Criteria

1. THE API_Gateway SHALL route all incoming REST requests to the appropriate downstream microservice based on the URL path prefix.
2. THE API_Gateway SHALL validate the JWT on every request to protected endpoints before forwarding to downstream services.
3. WHEN a JWT is invalid or expired, THE API_Gateway SHALL return HTTP 401 without forwarding the request.
4. THE API_Gateway SHALL enforce rate limiting per authenticated user: maximum 1000 requests per minute per user.
5. WHEN the rate limit is exceeded, THE API_Gateway SHALL return HTTP 429 with a Retry-After header indicating when the limit resets.
6. THE API_Gateway SHALL forward the validated user identity (user ID, entity ID, profile ID, rights) as request headers to downstream services.
7. THE API_Gateway SHALL support App-Token validation for API client identification.
8. THE API_Gateway SHALL expose a health check endpoint at GET /actuator/health returning the aggregated health of all downstream services.
9. THE API_Gateway SHALL log all incoming requests with: timestamp, method, path, user ID, response status, and latency in milliseconds.
10. THE API_Gateway SHALL support CORS configuration with configurable allowed origins, methods, and headers.

---

### Requirement 19: REST API Design — CRUD Operations

**User Story:** As an API client, I want consistent REST endpoints for all ITIL objects and assets, so that I can integrate with the backend using standard HTTP conventions.

#### Acceptance Criteria

1. THE System SHALL expose REST endpoints following the pattern: GET /{resource}, GET /{resource}/{id}, POST /{resource}, PUT /{resource}/{id}, PATCH /{resource}/{id}, DELETE /{resource}/{id}.
2. THE System SHALL return HTTP 200 for successful GET requests, HTTP 201 for successful POST (resource creation), HTTP 200 for successful PUT/PATCH, and HTTP 204 for successful DELETE.
3. THE System SHALL return HTTP 404 when a requested resource does not exist.
4. THE System SHALL return HTTP 403 when the authenticated user lacks the required right for the requested operation.
5. THE System SHALL return HTTP 422 with a structured error body when request validation fails, listing all validation errors.
6. THE System SHALL support pagination on all collection endpoints using query parameters: page (0-indexed, default 0) and size (default 50, max 500).
7. THE System SHALL return pagination metadata in the response body: totalElements, totalPages, currentPage, pageSize.
8. THE System SHALL support sorting on collection endpoints using query parameters: sort (field name) and order (ASC or DESC, default ASC).
9. THE System SHALL support field filtering on collection endpoints using query parameters matching field names and values.
10. THE System SHALL return all timestamps in ISO 8601 format (UTC).
11. THE System SHALL support the expand_dropdowns query parameter to return human-readable names instead of IDs for reference fields.
12. THE System SHALL support bulk operations: POST /{resource}/bulk for creating multiple resources in a single request (max 100 items per batch).

---

### Requirement 20: REST API Design — Sub-Resources

**User Story:** As an API client, I want to access sub-resources of ITIL objects (followups, tasks, actors, solutions) via nested REST endpoints, so that I can manage the full ticket lifecycle through the API.

#### Acceptance Criteria

1. THE Ticket_Service SHALL expose sub-resource endpoints: GET /tickets/{id}/followups, POST /tickets/{id}/followups, GET /tickets/{id}/tasks, POST /tickets/{id}/tasks, GET /tickets/{id}/solutions, POST /tickets/{id}/solutions, GET /tickets/{id}/actors, POST /tickets/{id}/actors, DELETE /tickets/{id}/actors/{actorId}.
2. THE Ticket_Service SHALL expose validation endpoints: GET /tickets/{id}/validations, POST /tickets/{id}/validations, PUT /tickets/{id}/validations/{validationId}.
3. THE Problem_Service SHALL expose sub-resource endpoints for followups, tasks, solutions, actors, and linked tickets.
4. THE Change_Service SHALL expose sub-resource endpoints for followups, tasks, solutions, actors, linked tickets, linked problems, and validation steps.
5. THE Asset_Service SHALL expose sub-resource endpoints for: GET /assets/{type}/{id}/networkports, GET /assets/computers/{id}/software, GET /assets/computers/{id}/devices, GET /assets/{type}/{id}/tickets.

---

### Requirement 21: Event-Driven Architecture — Kafka Topics

**User Story:** As a system architect, I want all domain events to be published to well-defined Kafka topics, so that services can react to changes asynchronously without tight coupling.

#### Acceptance Criteria

1. THE System SHALL publish domain events to the following Kafka topics: identity.users, identity.entities, identity.profiles, tickets.events, problems.events, changes.events, assets.events, sla.events, notifications.outbound, knowledge.events.
2. WHEN a domain event is published to Kafka, THE System SHALL include the following envelope fields: eventId (UUID), eventType (string), aggregateId (string), aggregateType (string), occurredAt (ISO 8601 timestamp), version (integer), and payload (JSON object).
3. THE System SHALL use the aggregate ID as the Kafka message key to ensure ordered delivery of events for the same aggregate.
4. THE System SHALL configure Kafka topics with a minimum of 3 partitions and a replication factor of 3 for production deployments.
5. THE Notification_Service SHALL consume from tickets.events, problems.events, changes.events, and sla.events topics using a dedicated consumer group.
6. WHEN a Kafka consumer fails to process a message after 3 retries, THE System SHALL route the message to a dead-letter topic ({topic}.dlq) and publish an alert.
7. THE System SHALL support at-least-once delivery semantics for all domain events, with idempotency keys to handle duplicate processing.

---

### Requirement 22: Data Requirements — MongoDB Document Models

**User Story:** As a backend developer, I want well-defined MongoDB document schemas for each aggregate, so that data is stored consistently and queries are efficient.

#### Acceptance Criteria

1. THE Identity_Service SHALL store User documents in the "users" collection with the following top-level fields: id, username, passwordHash, authType, authSourceId, emails (array), isActive, isDeleted, entityId, profileId, language, personalToken (encrypted), apiToken (encrypted), totpSecret (encrypted), createdAt, updatedAt.
2. THE Identity_Service SHALL store Entity documents in the "entities" collection with: id, name, parentId, level, completeName, config (embedded document with all entity configuration fields), createdAt, updatedAt.
3. THE Ticket_Service SHALL store Ticket documents in the "tickets" collection with: id, type, status, title, content, entityId, priority, urgency, impact, category, actors (embedded array), followups (embedded array), tasks (embedded array), solution (embedded document), sla (embedded document with deadlines), createdAt, updatedAt, solvedAt, closedAt, isDeleted.
4. THE Problem_Service SHALL store Problem documents in the "problems" collection with: id, status, title, content, entityId, priority, urgency, impact, actors (embedded array), linkedTicketIds (array), linkedAssets (array), impactContent, causeContent, symptomContent, createdAt, updatedAt.
5. THE Change_Service SHALL store Change documents in the "changes" collection with: id, status, title, content, entityId, priority, urgency, impact, actors (embedded array), planningDocuments (embedded document), validationSteps (embedded array), linkedTicketIds, linkedProblemIds, linkedAssets, createdAt, updatedAt.
6. THE Asset_Service SHALL store Asset documents in a polymorphic "assets" collection with: id, assetType (discriminator), name, entityId, serial, otherSerial, stateId, locationId, userId, groupId, manufacturerId, modelId, isDeleted, createdAt, updatedAt, plus type-specific embedded documents.
7. THE SLA_Service SHALL store SLA documents in the "slas" collection with: id, name, entityId, type (TTO/TTR), durationSeconds, calendarId, levels (embedded array of escalation levels), createdAt, updatedAt.
8. THE Knowledge_Service SHALL store KnowbaseItem documents in the "knowledge_articles" collection with: id, title, answer, authorId, isFaq, viewCount, visibility (embedded document with user/group/profile/entity rules), categories (array), revisions (array of revision references), beginDate, endDate, createdAt, updatedAt.
9. THE System SHALL create MongoDB indexes on all foreign key fields used in queries (entityId, userId, status, type, createdAt) to ensure query performance.
10. THE System SHALL use MongoDB transactions for operations that span multiple collections within the same service.

---

### Requirement 23: Non-Functional Requirements — Performance

**User Story:** As a system operator, I want the backend to handle high request volumes with low latency, so that the system remains responsive under production load.

#### Acceptance Criteria

1. THE System SHALL respond to 95% of GET requests for single resources within 200ms under a load of 500 concurrent users.
2. THE System SHALL respond to 95% of POST/PUT requests within 500ms under a load of 500 concurrent users.
3. THE System SHALL support a minimum throughput of 1000 requests per second across all services combined.
4. THE Ticket_Service SHALL process ticket creation (including SLA computation and Kafka event publication) within 1 second end-to-end.
5. THE SLA_Service SHALL complete the SLA deadline computation for a single ticket within 50ms.
6. THE System SHALL use Java 21 virtual threads (Project Loom) for all I/O-bound operations to maximize throughput without blocking platform threads.

---

### Requirement 24: Non-Functional Requirements — Scalability

**User Story:** As a system operator, I want each microservice to scale independently, so that I can allocate resources based on actual demand per service.

#### Acceptance Criteria

1. THE System SHALL be stateless at the application layer, storing all session state in JWT tokens or distributed cache, so that any instance can handle any request.
2. THE System SHALL support horizontal scaling of each microservice independently via container orchestration (Docker/Kubernetes).
3. THE Notification_Service SHALL scale consumer instances independently by adding Kafka consumer group members without redeployment.
4. THE System SHALL use MongoDB connection pooling with a configurable pool size (default: 10 connections per service instance).
5. THE System SHALL support a minimum of 10 concurrent service instances per microservice without data consistency issues.

---

### Requirement 25: Non-Functional Requirements — Security

**User Story:** As a security officer, I want the backend to enforce security best practices at every layer, so that sensitive data is protected and unauthorized access is prevented.

#### Acceptance Criteria

1. THE System SHALL encrypt all sensitive fields at rest: passwords (bcrypt, cost factor ≥ 12), API tokens (AES-256), TOTP secrets (AES-256).
2. THE System SHALL enforce HTTPS (TLS 1.2+) for all external communications.
3. THE System SHALL validate all input data against defined schemas before processing, rejecting requests with unexpected fields or types.
4. THE System SHALL not include sensitive fields (password, tokens, TOTP secrets) in any API response or log output.
5. THE System SHALL implement OWASP Top 10 protections: SQL/NoSQL injection prevention, XSS prevention in stored content, CSRF protection on state-changing endpoints.
6. THE System SHALL log all authentication events (login, logout, failed attempts, token refresh) with user ID, IP address, and timestamp.
7. WHEN 5 consecutive failed login attempts occur for the same username within 10 minutes, THE Identity_Service SHALL temporarily lock the account for 15 minutes and publish an AccountLocked domain event.
8. THE System SHALL support configurable CORS policies per environment (development vs. production).

---

### Requirement 26: Non-Functional Requirements — Availability and Observability

**User Story:** As a system operator, I want the backend to be highly available and observable, so that I can detect and resolve issues quickly.

#### Acceptance Criteria

1. THE System SHALL expose Spring Boot Actuator health endpoints at /actuator/health for each service, returning the health of MongoDB and Kafka connections.
2. THE System SHALL expose Prometheus-compatible metrics at /actuator/prometheus for each service, including: request count, request latency (p50, p95, p99), error rate, Kafka consumer lag, and MongoDB connection pool usage.
3. THE System SHALL emit structured JSON logs with the following fields: timestamp, level, service, traceId, spanId, userId, message.
4. THE System SHALL propagate distributed trace IDs (W3C Trace Context) across all service-to-service calls and Kafka messages.
5. THE System SHALL implement circuit breakers on all inter-service HTTP calls with a configurable failure threshold (default: 50% failure rate over 10 seconds).
6. WHEN a circuit breaker opens, THE System SHALL return a fallback response (HTTP 503 with a descriptive error) rather than propagating the failure.

---

### Requirement 27: Non-Functional Requirements — Containerization

**User Story:** As a DevOps engineer, I want each microservice to be containerized with Docker and orchestrated with Docker Compose for local development, so that the full stack can be started with a single command.

#### Acceptance Criteria

1. THE System SHALL provide a Dockerfile for each microservice using a multi-stage build: build stage (Maven/Gradle) and runtime stage (Eclipse Temurin JRE 21 slim).
2. THE System SHALL provide a docker-compose.yml file that starts all microservices, MongoDB, Kafka, Zookeeper, and the API Gateway.
3. THE System SHALL configure each service's Docker image to run as a non-root user.
4. THE System SHALL expose each service on a distinct port in the Docker Compose configuration to avoid conflicts.
5. THE System SHALL provide environment variable configuration for all sensitive settings (database URIs, JWT keys, Kafka brokers) with documented defaults for local development.

---

### Requirement 28: ITIL Compliance — Status Transition Rules

**User Story:** As an ITIL process owner, I want status transitions to be enforced according to ITIL best practices, so that the system prevents invalid workflow states.

#### Acceptance Criteria

1. THE Ticket_Service SHALL enforce that a ticket in CLOSED status cannot be directly transitioned to SOLVED without first being reopened to INCOMING or ASSIGNED.
2. THE Ticket_Service SHALL enforce that a ticket can only be transitioned to SOLVED if at least one ITILSolution exists.
3. THE Change_Service SHALL enforce that a change cannot transition from INCOMING directly to SOLVED, requiring passage through EVALUATION or APPROVAL.
4. THE Problem_Service SHALL enforce that a problem in CLOSED status can only be reopened to INCOMING or ACCEPTED.
5. THE System SHALL store the allowed status transition matrix per profile (ticket_status, problem_status, change_status fields) and enforce it on every status update.
6. WHEN a status transition is attempted that is not permitted by the profile's transition matrix, THE System SHALL return HTTP 422 with error code INVALID_STATUS_TRANSITION.

---

### Requirement 29: ITIL Compliance — Priority Matrix

**User Story:** As a support technician, I want the system to automatically compute ticket priority from impact and urgency, so that tickets are consistently prioritized according to ITIL standards.

#### Acceptance Criteria

1. THE Ticket_Service SHALL compute priority using the formula: priority = priority_matrix[urgency][impact], where the matrix is configurable per entity.
2. THE Ticket_Service SHALL use the following default priority matrix (urgency rows 1-5, impact columns 1-5): (1,1)=1, (1,2)=2, (1,3)=2, (1,4)=3, (1,5)=3, (2,1)=2, (2,2)=2, (2,3)=3, (2,4)=3, (2,5)=4, (3,1)=2, (3,2)=3, (3,3)=3, (3,4)=4, (3,5)=4, (4,1)=3, (4,2)=3, (4,3)=4, (4,4)=4, (4,5)=5, (5,1)=3, (5,2)=4, (5,3)=4, (5,4)=5, (5,5)=5.
3. THE Ticket_Service SHALL allow the priority matrix to be overridden per entity by an administrator with the appropriate rights.
4. WHEN urgency or impact is updated on a ticket, THE Ticket_Service SHALL automatically recompute the priority unless the priority was manually overridden by a user with CHANGEPRIORITY right.
5. THE Ticket_Service SHALL support masking urgency and impact values per entity (urgency_mask, impact_mask bitfields) to restrict which values are available to users.

---

### Requirement 30: Correctness Properties for Property-Based Testing

**User Story:** As a quality engineer, I want property-based tests to verify the correctness of core domain logic, so that edge cases and invariants are systematically validated.

#### Acceptance Criteria

**Priority Matrix Properties**

1. FOR ALL valid (urgency, impact) pairs where urgency ∈ [1,5] and impact ∈ [1,5], THE Ticket_Service priority computation SHALL return a value in the range [1,6].
2. FOR ALL valid (urgency, impact) pairs, THE Ticket_Service priority computation SHALL be monotonically non-decreasing: if urgency2 ≥ urgency1 and impact2 ≥ impact1, then priority(urgency2, impact2) ≥ priority(urgency1, impact1).
3. FOR ALL valid priority matrix configurations, THE Ticket_Service SHALL produce the same priority for the same (urgency, impact) pair regardless of the order in which the matrix was configured (idempotence).

**SLA Deadline Computation Properties**

4. FOR ALL valid (startDate, durationSeconds, calendarId) inputs where durationSeconds > 0, THE SLA_Service computeDeadline operation SHALL return a deadline strictly after the startDate.
5. FOR ALL valid (startDate, durationSeconds, calendarId) inputs, THE SLA_Service SHALL satisfy the round-trip property: computeRemainingBusinessSeconds(startDate, computeDeadline(startDate, durationSeconds, calendarId), calendarId) SHALL equal durationSeconds within a tolerance of 1 second.
6. FOR ALL valid calendar configurations, THE SLA_Service deadline computation SHALL be idempotent: computing the deadline twice with the same inputs SHALL return the same result.
7. FOR ALL valid (startDate, durationSeconds, calendarId) inputs where durationSeconds2 > durationSeconds1, THE SLA_Service SHALL satisfy the ordering property: computeDeadline(startDate, durationSeconds2, calendarId) > computeDeadline(startDate, durationSeconds1, calendarId).

**Status Transition Properties**

8. FOR ALL valid ticket status transitions, THE Ticket_Service SHALL satisfy the invariant: a ticket in CLOSED status SHALL NOT transition directly to SOLVED without passing through INCOMING or ASSIGNED.
9. FOR ALL sequences of valid status transitions applied to a ticket, THE Ticket_Service SHALL satisfy the invariant: the ticket's status history SHALL be a valid path in the allowed transition graph.
10. FOR ALL ticket status values, THE Ticket_Service SHALL satisfy the idempotence property: applying the same status transition twice SHALL result in the same final state as applying it once (no duplicate events).

**Actor Management Properties**

11. FOR ALL valid actor addition operations on a ticket, THE Ticket_Service SHALL satisfy the invariant: the set of actors after addition SHALL be a superset of the actors before addition.
12. FOR ALL valid actor removal operations on a ticket, THE Ticket_Service SHALL satisfy the invariant: the removed actor SHALL NOT appear in the actor list after removal.
13. FOR ALL sequences of actor add/remove operations, THE Ticket_Service SHALL satisfy the confluence property: the final actor set SHALL be the same regardless of the order in which independent add/remove operations are applied.

**Entity Hierarchy Properties**

14. FOR ALL valid entity trees, THE Identity_Service SHALL satisfy the invariant: the completeName of any entity SHALL be the concatenation of all ancestor names separated by " > ", ending with the entity's own name.
15. FOR ALL valid entity trees, THE Identity_Service SHALL satisfy the invariant: no entity SHALL be its own ancestor (no cycles in the entity tree).
16. FOR ALL entity configuration inheritance chains, THE Identity_Service SHALL satisfy the round-trip property: resolving an inherited configuration value and then setting it explicitly SHALL produce the same effective value as setting it directly.

**Notification Deduplication Properties**

17. FOR ALL domain events published to Kafka, THE Notification_Service SHALL satisfy the idempotence property: processing the same event twice (due to at-least-once delivery) SHALL result in exactly one notification being dispatched per target.

**JSON Serialization Round-Trip Properties**

18. FOR ALL valid Ticket domain objects, THE Ticket_Service SHALL satisfy the round-trip property: serialize(deserialize(serialize(ticket))) SHALL equal serialize(ticket) (JSON serialization idempotence).
19. FOR ALL valid SLA domain objects, THE SLA_Service SHALL satisfy the round-trip property: deserialize(serialize(sla)) SHALL produce an SLA object equal to the original (field-by-field equality).
20. FOR ALL valid domain events published to Kafka, THE System SHALL satisfy the round-trip property: deserializing the Kafka message payload SHALL produce a domain event object equal to the one that was serialized before publishing.

---

## Summary of Requirements

| # | Requirement | Service | Type |
|---|---|---|---|
| 1 | User Lifecycle | Identity_Service | Functional |
| 2 | Entity Hierarchy | Identity_Service | Functional |
| 3 | Profiles and RBAC | Identity_Service | Functional |
| 4 | Authentication and JWT | Identity_Service / API_Gateway | Functional |
| 5 | Ticket Lifecycle | Ticket_Service | Functional |
| 6 | Ticket Actors | Ticket_Service | Functional |
| 7 | Followups, Tasks, Solutions | Ticket_Service | Functional |
| 8 | Validation Workflow | Ticket_Service | Functional |
| 9 | SLA Integration on Tickets | Ticket_Service / SLA_Service | Functional |
| 10 | Problem Lifecycle | Problem_Service | Functional |
| 11 | Change Lifecycle | Change_Service | Functional |
| 12 | Asset Lifecycle | Asset_Service | Functional |
| 13 | Software and License Management | Asset_Service | Functional |
| 14 | SLA/OLA Definitions | SLA_Service | Functional |
| 15 | SLA Escalation | SLA_Service | Functional |
| 16 | Notification Service | Notification_Service | Functional |
| 17 | Knowledge Base | Knowledge_Service | Functional |
| 18 | API Gateway | API_Gateway | Functional |
| 19 | REST API CRUD | All Services | Functional |
| 20 | REST API Sub-Resources | Ticket/Problem/Change/Asset Services | Functional |
| 21 | Kafka Topics | All Services | Functional |
| 22 | MongoDB Document Models | All Services | Data |
| 23 | Performance | All Services | Non-Functional |
| 24 | Scalability | All Services | Non-Functional |
| 25 | Security | All Services | Non-Functional |
| 26 | Availability and Observability | All Services | Non-Functional |
| 27 | Containerization | All Services | Non-Functional |
| 28 | ITIL Status Transitions | Ticket/Problem/Change Services | ITIL Compliance |
| 29 | ITIL Priority Matrix | Ticket_Service | ITIL Compliance |
| 30 | Correctness Properties (PBT) | All Services | Quality |
