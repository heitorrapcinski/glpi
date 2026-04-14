# Requirements Document

## Introduction

This document defines the functional and non-functional requirements for building the GLPI PWA Frontend MVP. The frontend is a Progressive Web Application (PWA) built with React and HTML5 that consumes the GLPI Microservices Backend (spec 2026.000001). The visual design must maintain maximum similarity with the original GLPI v12.0.0 application (located in `.legacy/` — read-only reference), replicating its layout structure, color palette, navigation patterns, and ITIL object presentation.

The MVP covers the following functional areas: authentication, dual-interface navigation (central and helpdesk), ticket management (full ITIL lifecycle), problem management, change management, asset/CMDB browsing, knowledge base, user preferences, entity/profile switching, global search, and dashboard views. Administration panels (setup, rules, dictionaries) are out of scope for this MVP.

The frontend runs in the browser on desktop and mobile devices and can be installed as a standalone app via PWA capabilities (service worker, web app manifest).

---

## Glossary

- **PWA_App**: The Progressive Web Application frontend that runs in the browser and can be installed as a standalone app.
- **API_Gateway**: The backend edge service at port 8080 that routes requests, validates JWT tokens, and enforces rate limiting.
- **Central_Interface**: The full ITSM interface for technicians, administrators, and supervisors with access to all modules.
- **Helpdesk_Interface**: The self-service interface for end users with limited access to ticket creation, FAQ, and personal ticket tracking.
- **Sidebar**: The vertical navigation panel on the left side of the Central_Interface containing the main menu sections.
- **Top_Bar**: The horizontal header bar containing breadcrumbs, global search, user menu, and profile/entity selector.
- **Timeline**: The chronological display of followups, tasks, solutions, and log entries on an ITIL object detail page.
- **Fields_Panel**: The right-side collapsible panel on ITIL object detail pages showing metadata fields (status, priority, actors, SLA, category).
- **Search_Engine**: The configurable list view with column selection, filtering, sorting, and pagination for browsing collections of objects.
- **Dashboard**: A configurable panel displaying summary widgets (counters, charts) for ITIL objects.
- **Theme**: A named color palette that controls the visual appearance of the PWA_App (sidebar color, accent color, background tones).
- **Service_Worker**: A background script that enables offline caching, push notifications, and PWA install capabilities.
- **JWT**: JSON Web Token used for stateless authentication with the API_Gateway.
- **Rich_Text_Editor**: A WYSIWYG editor component for composing followups, tasks, solutions, and knowledge articles.
- **Actor**: A user, group, or supplier assigned to an ITIL object in one of four roles: requester, assigned, observer, or supplier.

---

## Requirements

### Requirement 1: Authentication — Login and Session Management

**User Story:** As an API client user, I want to log in with my credentials and maintain a secure session, so that I can access the GLPI system through the frontend.

#### Acceptance Criteria

1. THE PWA_App SHALL present a login page with username and password fields, visually matching the legacy GLPI login card layout (centered card with GLPI logo, form fields, and sign-in button).
2. WHEN valid credentials are submitted, THE PWA_App SHALL send a POST request to the API_Gateway at /auth/login and store the returned JWT access token and refresh token securely in memory.
3. WHEN the JWT access token expires, THE PWA_App SHALL automatically request a new access token using the refresh token via POST /auth/refresh without interrupting the user session.
4. WHEN the refresh token is invalid or expired, THE PWA_App SHALL redirect the user to the login page and clear all stored tokens.
5. WHEN the user clicks the logout button, THE PWA_App SHALL send a POST request to /auth/logout, clear all stored tokens, and redirect to the login page.
6. THE PWA_App SHALL include the JWT access token in the Authorization header (Bearer scheme) on every API request to the API_Gateway.
7. WHEN the API_Gateway returns HTTP 401 on any request, THE PWA_App SHALL attempt a token refresh and retry the request once before redirecting to the login page.
8. IF the login request returns HTTP 401 with an account locked error, THEN THE PWA_App SHALL display a message indicating the account is temporarily locked.
9. THE PWA_App SHALL support a "Remember me" checkbox that persists the refresh token in localStorage when checked.
10. WHEN Two-Factor Authentication is required (HTTP 403 with 2FA_REQUIRED code), THE PWA_App SHALL present a TOTP code input field for the user to complete authentication.

---

### Requirement 2: Authentication — Profile and Entity Switching

**User Story:** As a user with multiple profiles and entity assignments, I want to switch my active profile and entity without logging out, so that I can work in different organizational contexts.

#### Acceptance Criteria

1. THE PWA_App SHALL display the current active profile name and entity name in the Top_Bar user menu, matching the legacy layout (profile name above, entity name below with truncation).
2. WHEN the user opens the profile selector dropdown, THE PWA_App SHALL fetch and display all profiles assigned to the user from the API_Gateway.
3. WHEN the user selects a different profile, THE PWA_App SHALL send a request to switch the active profile and obtain a new JWT reflecting the updated profile rights.
4. WHEN the user opens the entity selector, THE PWA_App SHALL display the entity tree hierarchy allowing the user to select any entity they have access to.
5. WHEN the user selects a different entity, THE PWA_App SHALL send a request to switch the active entity and obtain a new JWT reflecting the updated entity context.
6. THE PWA_App SHALL reload the current view after a profile or entity switch to reflect the new permissions and data scope.

---

### Requirement 3: Navigation — Central Interface Layout

**User Story:** As a technician or administrator, I want a navigation layout matching the legacy GLPI central interface, so that I can access all ITSM modules through a familiar sidebar and top bar.

#### Acceptance Criteria

1. THE PWA_App SHALL render a vertical Sidebar on the left side for the Central_Interface, containing the GLPI logo at the top and collapsible menu sections below, matching the legacy layout structure.
2. THE Sidebar SHALL contain the following top-level menu sections matching the legacy GLPI menu: Assets, Assistance, Management, Tools, and Administration (Administration is visible but displays a "coming soon" placeholder for MVP).
3. WHEN a top-level menu section is clicked, THE PWA_App SHALL expand a dropdown showing the sub-menu items with icons, matching the legacy multi-column dropdown layout.
4. THE PWA_App SHALL render a Top_Bar above the content area containing: breadcrumbs (left), global search input (center-right), and user menu with profile/entity selector (right).
5. THE Sidebar SHALL support a collapsed mode (icons only) toggled by a "Collapse menu" button at the bottom, persisting the preference in the user's local settings.
6. THE PWA_App SHALL apply the GLPI default color scheme to the Sidebar: background color #2f3f64 (dark navy), foreground color #f4f6fa (light gray), and active item highlight using a lighter variant.
7. THE PWA_App SHALL use the GLPI primary accent color rgb(254, 201, 92) (golden yellow) for primary buttons and interactive highlights, matching the legacy Tabler-based design tokens.
8. THE PWA_App SHALL render the content area to the right of the Sidebar with a light background (#f5f7fb) matching the legacy body background.

---

### Requirement 4: Navigation — Helpdesk Interface Layout

**User Story:** As an end user with a helpdesk profile, I want a simplified self-service interface, so that I can create tickets, browse FAQ, and track my requests without the complexity of the full ITSM interface.

#### Acceptance Criteria

1. WHEN the active profile has interface type "helpdesk", THE PWA_App SHALL render the Helpdesk_Interface layout with a horizontal top navigation bar (no vertical Sidebar).
2. THE Helpdesk_Interface SHALL display a search banner at the top with a large search input for finding FAQ articles and knowledge base content, matching the legacy helpdesk home layout.
3. THE Helpdesk_Interface SHALL display tile cards below the search banner for quick actions: "Create a Ticket", "My Tickets", and "FAQ".
4. THE Helpdesk_Interface SHALL display a tabbed section showing the user's open tickets and recently solved tickets.
5. THE PWA_App SHALL restrict the Helpdesk_Interface navigation to only the modules permitted by the helpdesk profile rights (tickets, FAQ, personal settings).

---

### Requirement 5: Ticket Management — List View

**User Story:** As a technician, I want to browse tickets in a configurable list view with filtering, sorting, and pagination, so that I can find and manage tickets efficiently.

#### Acceptance Criteria

1. THE PWA_App SHALL render a Search_Engine table for tickets at the /tickets route, displaying columns matching the legacy GLPI search display (ID, status icon, title, requester, assigned, priority, last update, entity).
2. THE Search_Engine SHALL support pagination with configurable page size (default 50), displaying pagination metadata (current page, total pages, total elements) in a footer bar matching the legacy pager component.
3. THE Search_Engine SHALL support column sorting by clicking column headers, sending sort and order query parameters to the API_Gateway.
4. THE Search_Engine SHALL support filtering by status, priority, entity, assigned user, and category using filter controls above the table.
5. WHEN a ticket row is clicked, THE PWA_App SHALL navigate to the ticket detail page at /tickets/{id}.
6. THE Search_Engine SHALL display ticket status using colored status badges matching the legacy GLPI status color scheme (new=blue, assigned=orange, planned=purple, waiting=yellow, solved=green, closed=gray).
7. THE Search_Engine SHALL display ticket priority using colored priority indicators matching the legacy GLPI priority colors (very low=gray, low=blue, medium=orange, high=red, very high=dark red, major=black).
8. THE PWA_App SHALL support bulk actions on selected tickets (change status, assign, delete) via checkboxes and a bulk action toolbar, matching the legacy massive action pattern.

---

### Requirement 6: Ticket Management — Detail View

**User Story:** As a technician, I want to view and edit a ticket with its full timeline and metadata fields, so that I can manage the ticket lifecycle from a single page.

#### Acceptance Criteria

1. THE PWA_App SHALL render the ticket detail page with a two-column layout matching the legacy ITIL object layout: Timeline on the left (8/12 width) and Fields_Panel on the right (4/12 width).
2. THE Timeline SHALL display all followups, tasks, solutions, and log entries in chronological order (configurable: newest first or oldest first), each with a colored left border matching the legacy timeline colors (followup=#ececec, task=#ffe8b9, solution=#9fd6ed, document=#80cead).
3. THE Fields_Panel SHALL display ticket metadata in collapsible accordion sections: Status, Dates, Actors (requester, assigned, observer), Priority/Urgency/Impact, Category, SLA information, and linked items.
4. THE Fields_Panel SHALL support a collapsed mode (hidden) and an expanded mode (wider right panel), toggled by collapse/expand buttons matching the legacy panel width switcher.
5. WHEN the user updates a field in the Fields_Panel (status, priority, category, actors), THE PWA_App SHALL send a PATCH request to the API_Gateway and refresh the ticket data on success.
6. THE PWA_App SHALL display the ticket title and ID in a header above the two-column layout.
7. THE PWA_App SHALL render actor badges in the Fields_Panel showing user avatars or initials, matching the legacy actor display pattern.
8. THE PWA_App SHALL display SLA deadline information with visual indicators (green=on track, orange=approaching, red=breached) in the Fields_Panel.

---

### Requirement 7: Ticket Management — Timeline Actions

**User Story:** As a technician, I want to add followups, tasks, and solutions to a ticket from the timeline, so that I can document the resolution process.

#### Acceptance Criteria

1. THE PWA_App SHALL render action buttons at the bottom of the Timeline for adding: Followup, Task, Solution, and Document, matching the legacy timeline footer button layout.
2. WHEN the user clicks "Add Followup", THE PWA_App SHALL display an inline form with a Rich_Text_Editor and a private/public toggle, matching the legacy followup form.
3. WHEN the user clicks "Add Task", THE PWA_App SHALL display an inline form with: Rich_Text_Editor for content, assigned user selector, planned start/end date pickers, duration input, and status selector (TODO/DONE).
4. WHEN the user clicks "Add Solution", THE PWA_App SHALL display an inline form with a Rich_Text_Editor for the solution content and a solution type selector.
5. WHEN a followup, task, or solution form is submitted, THE PWA_App SHALL send a POST request to the appropriate sub-resource endpoint on the API_Gateway and append the new entry to the Timeline on success.
6. THE Rich_Text_Editor SHALL support basic formatting (bold, italic, underline, lists, links, code blocks) and image/file attachments.
7. WHEN a solution is pending approval, THE PWA_App SHALL display approve/reject buttons on the solution timeline entry for users with validation rights.

---

### Requirement 8: Ticket Management — Creation

**User Story:** As a user, I want to create a new ticket with all required fields, so that I can report incidents or submit service requests.

#### Acceptance Criteria

1. THE PWA_App SHALL render a ticket creation form at /tickets/new with fields matching the legacy new ticket form: type selector (Incident/Request), title, content (Rich_Text_Editor), category, urgency, entity, and actor fields (requester, assigned, observer).
2. THE PWA_App SHALL auto-populate the requester field with the current logged-in user.
3. WHEN the ticket type is selected, THE PWA_App SHALL filter the category dropdown to show only categories applicable to the selected type.
4. WHEN the form is submitted, THE PWA_App SHALL send a POST request to the API_Gateway at /tickets and navigate to the newly created ticket detail page on success.
5. IF the API_Gateway returns HTTP 422 with validation errors, THEN THE PWA_App SHALL display the error messages inline next to the corresponding form fields.
6. THE PWA_App SHALL support the urgency field with values filtered by the entity's urgency mask configuration.
7. WHEN the Helpdesk_Interface is active, THE PWA_App SHALL render a simplified ticket creation form showing only: title, content, urgency, and category.

---

### Requirement 9: Problem Management — List and Detail Views

**User Story:** As a problem manager, I want to browse and manage ITIL problems with the same UI patterns as tickets, so that I have a consistent experience across ITIL modules.

#### Acceptance Criteria

1. THE PWA_App SHALL render a Search_Engine table for problems at the /problems route with columns: ID, status, title, priority, assigned, entity, linked tickets count, and last update.
2. THE PWA_App SHALL render the problem detail page with the same two-column layout as tickets (Timeline left, Fields_Panel right).
3. THE Fields_Panel for problems SHALL include additional sections for: impact analysis, root cause analysis, and symptom description, each with a Rich_Text_Editor.
4. THE PWA_App SHALL display linked tickets on the problem detail page with clickable links navigating to the ticket detail view.
5. THE PWA_App SHALL support creating new problems at /problems/new with fields: title, content, urgency, impact, category, actors, and linked tickets.
6. THE PWA_App SHALL support adding followups, tasks, and solutions to problems using the same Timeline action pattern as tickets.

---

### Requirement 10: Change Management — List and Detail Views

**User Story:** As a change manager, I want to browse and manage ITIL changes with extended status workflows and planning documents, so that I can control the change lifecycle.

#### Acceptance Criteria

1. THE PWA_App SHALL render a Search_Engine table for changes at the /changes route with columns: ID, status, title, priority, assigned, entity, and last update.
2. THE PWA_App SHALL render the change detail page with the same two-column layout as tickets (Timeline left, Fields_Panel right).
3. THE Fields_Panel for changes SHALL include additional sections for planning documents: impact analysis, control list, rollout plan, backout plan, and checklist, each with a Rich_Text_Editor.
4. THE PWA_App SHALL display the change validation workflow in the Fields_Panel, showing each validation step with its status (waiting, approved, refused) and validator name.
5. WHEN a user with validation rights views a pending validation step, THE PWA_App SHALL display approve/reject buttons with a comment field.
6. THE PWA_App SHALL support creating new changes at /changes/new with fields: title, content, urgency, impact, category, actors, and linked tickets/problems.
7. THE PWA_App SHALL display linked tickets and linked problems on the change detail page with clickable navigation links.
8. THE PWA_App SHALL support adding followups, tasks, and solutions to changes using the same Timeline action pattern as tickets.

---

### Requirement 11: Asset/CMDB — Asset List and Detail Views

**User Story:** As an IT asset manager, I want to browse and view configuration items in the CMDB, so that I can track the organization's IT inventory.

#### Acceptance Criteria

1. THE PWA_App SHALL render a Search_Engine table for assets at the /assets route, with a type filter tab bar (All, Computers, Network Equipment, Monitors, Printers, Phones, Peripherals, Software) matching the legacy asset navigation.
2. THE Search_Engine for assets SHALL display columns: ID, name, type, status, location, assigned user, entity, serial number, and last update.
3. THE PWA_App SHALL render the asset detail page with tabbed sections matching the legacy asset form: General Information, Components (for computers), Software (for computers), Network Ports, Financial Information, Contracts, Linked Tickets, and History.
4. THE General Information tab SHALL display asset fields in a form layout: name, type, status, location, assigned user, assigned group, manufacturer, model, serial number, and inventory number.
5. THE Components tab (computers only) SHALL display hardware components (CPU, RAM, HDD, GPU) in a table format.
6. THE Network Ports tab SHALL display network port entries with: port name, MAC address, IP address, VLAN, and connection type.
7. THE Financial Information tab SHALL display Infocom fields: purchase date, purchase price, warranty expiry, order number, delivery date, and depreciation data.
8. THE PWA_App SHALL support navigating from an asset to its linked tickets, problems, and changes via clickable links.

---

### Requirement 12: Asset/CMDB — Software and License Views

**User Story:** As an IT asset manager, I want to view software installations and license compliance, so that I can monitor licensing status.

#### Acceptance Criteria

1. THE PWA_App SHALL render a Search_Engine table for software at the /assets/software route with columns: name, manufacturer, category, number of installations, and number of licenses.
2. THE PWA_App SHALL render a Search_Engine table for software licenses at the /assets/licenses route with columns: name, software, license type, serial, total seats, used seats, remaining seats, and expiry date.
3. THE PWA_App SHALL display a license compliance indicator using color coding: green (seats available), orange (80% or more used), red (over-licensed or expired).
4. THE PWA_App SHALL render the software detail page showing: general information, version list, and installations per version.

---

### Requirement 13: Knowledge Base — Article Browsing and Search

**User Story:** As a user, I want to browse and search knowledge base articles, so that I can find solutions to common problems.

#### Acceptance Criteria

1. THE PWA_App SHALL render a knowledge base browsing page at /knowledge with a category tree on the left and article list on the right.
2. THE PWA_App SHALL support full-text search on knowledge base articles using a search input at the top of the article list.
3. THE PWA_App SHALL render the article detail page displaying: title, content (rendered rich text), author, creation date, last update, view count, and linked items.
4. THE PWA_App SHALL display FAQ articles with a distinct visual indicator (FAQ badge) in the article list.
5. WHEN the Helpdesk_Interface is active, THE PWA_App SHALL display only FAQ articles visible to the user's entity and profile.
6. THE PWA_App SHALL support navigating from a knowledge article to its linked tickets, problems, and changes.
7. THE PWA_App SHALL increment the article view counter by sending a view event to the API_Gateway when an article detail page is loaded.

---

### Requirement 14: Global Search

**User Story:** As a user, I want to search across all ITIL objects and assets from a single search input, so that I can quickly find any item in the system.

#### Acceptance Criteria

1. THE PWA_App SHALL render a global search input in the Top_Bar that searches across tickets, problems, changes, assets, and knowledge articles.
2. WHEN the user types in the global search input, THE PWA_App SHALL display a dropdown with categorized results (grouped by object type) after a debounce delay of 300 milliseconds.
3. WHEN the user selects a search result, THE PWA_App SHALL navigate to the detail page of the selected item.
4. THE PWA_App SHALL display up to 5 results per category in the search dropdown, with a "View all" link to the full search results page.
5. THE global search dropdown SHALL display each result with: object type icon, ID, title, and status badge.

---

### Requirement 15: Dashboard

**User Story:** As a technician or manager, I want to view summary dashboards with counters and charts, so that I can monitor the current state of ITIL operations at a glance.

#### Acceptance Criteria

1. THE PWA_App SHALL render a dashboard page at the /dashboard route as the default landing page for the Central_Interface.
2. THE Dashboard SHALL display summary widgets including: open tickets count, tickets by status (bar chart), tickets by priority (pie chart), my assigned tickets count, overdue tickets count, and recent activity feed.
3. THE Dashboard widgets SHALL fetch data from the API_Gateway using the collection endpoints with appropriate filters and aggregation parameters.
4. THE PWA_App SHALL render dashboard widgets using a responsive grid layout that adapts to screen size.
5. WHEN a dashboard counter widget is clicked, THE PWA_App SHALL navigate to the corresponding filtered list view (e.g., clicking "Overdue Tickets" navigates to /tickets?status=overdue).

---

### Requirement 16: User Preferences

**User Story:** As a user, I want to configure my personal preferences (language, theme, layout, timeline order), so that the interface matches my working style.

#### Acceptance Criteria

1. THE PWA_App SHALL render a user preferences page at /preferences accessible from the user menu dropdown.
2. THE preferences page SHALL support configuring: display language, theme/palette selection, page layout (vertical sidebar or horizontal top bar), timeline order (newest first or oldest first), number of items per page, and sidebar collapsed state.
3. WHEN the user changes the theme/palette, THE PWA_App SHALL apply the selected color palette immediately by updating CSS custom properties, matching the legacy GLPI palette system (classic, dark, darker, midnight, auror, teclib, and other palettes).
4. THE PWA_App SHALL persist user preferences locally and sync them with the backend user settings API when available.
5. THE PWA_App SHALL apply the default GLPI palette (classic light theme) when no user preference is set.

---

### Requirement 17: Progressive Web App Capabilities

**User Story:** As a user, I want to install the GLPI frontend as a standalone app on my device and receive basic offline support, so that I can access it like a native application.

#### Acceptance Criteria

1. THE PWA_App SHALL include a valid web app manifest (manifest.json) with: app name "GLPI", short name "GLPI", start URL, display mode "standalone", theme color #2f3f64, background color #f5f7fb, and app icons in multiple sizes (192x192, 512x512).
2. THE PWA_App SHALL register a Service_Worker that caches the application shell (HTML, CSS, JavaScript, fonts, icons) for offline loading of the app skeleton.
3. WHEN the PWA_App is offline, THE Service_Worker SHALL serve the cached application shell and display a user-friendly offline indicator banner.
4. THE PWA_App SHALL be installable as a standalone app on supported browsers (Chrome, Edge, Safari) via the browser's "Add to Home Screen" or "Install App" prompt.
5. THE Service_Worker SHALL use a cache-first strategy for static assets and a network-first strategy for API requests.
6. THE PWA_App SHALL display an update notification banner when a new version of the Service_Worker is available, prompting the user to refresh.

---

### Requirement 18: Responsive Design

**User Story:** As a user, I want the frontend to work on both desktop and mobile devices, so that I can manage ITIL operations from any screen size.

#### Acceptance Criteria

1. THE PWA_App SHALL implement a responsive layout that adapts to screen widths from 320px (mobile) to 2560px (ultra-wide desktop).
2. WHEN the viewport width is below 992px (tablet breakpoint), THE PWA_App SHALL collapse the Sidebar into a hamburger menu toggle, matching the legacy responsive behavior.
3. WHEN the viewport width is below 768px (mobile breakpoint), THE PWA_App SHALL stack the ticket detail two-column layout into a single column with the Fields_Panel below the Timeline.
4. THE PWA_App SHALL use touch-friendly interaction targets (minimum 44x44px tap areas) on mobile viewports.
5. THE Search_Engine tables SHALL support horizontal scrolling on narrow viewports to prevent content truncation.
6. THE PWA_App SHALL hide non-essential UI elements (breadcrumbs, secondary toolbars) on mobile viewports to maximize content area.

---

### Requirement 19: API Integration — HTTP Client

**User Story:** As a frontend developer, I want a centralized HTTP client module that handles all communication with the API_Gateway, so that authentication, error handling, and request formatting are consistent.

#### Acceptance Criteria

1. THE PWA_App SHALL implement a centralized HTTP client module that prepends the API_Gateway base URL to all requests and attaches the JWT Authorization header automatically.
2. THE HTTP client SHALL implement automatic token refresh when a request returns HTTP 401, retrying the original request with the new token.
3. WHEN the API_Gateway returns HTTP 429 (rate limited), THE HTTP client SHALL display a user-friendly notification indicating the rate limit and retry after the Retry-After duration.
4. THE HTTP client SHALL parse pagination metadata from API responses (totalElements, totalPages, currentPage, pageSize) and expose it to consuming components.
5. THE HTTP client SHALL handle network errors gracefully, displaying a connection error notification and offering a retry action.
6. THE HTTP client SHALL support the expand_dropdowns query parameter to resolve reference field IDs to human-readable names.

---

### Requirement 20: Routing and Code Splitting

**User Story:** As a frontend developer, I want the application to use client-side routing with lazy-loaded modules, so that the initial load is fast and navigation is seamless.

#### Acceptance Criteria

1. THE PWA_App SHALL implement client-side routing using React Router with the following top-level routes: /login, /dashboard, /tickets, /tickets/:id, /tickets/new, /problems, /problems/:id, /problems/new, /changes, /changes/:id, /changes/new, /assets, /assets/:type/:id, /knowledge, /knowledge/:id, /preferences.
2. THE PWA_App SHALL lazy-load route modules using React.lazy and Suspense, so that only the code for the current route is loaded on navigation.
3. THE PWA_App SHALL display a loading skeleton (matching the page layout structure) while a lazy-loaded route module is being fetched.
4. THE PWA_App SHALL protect all routes except /login with an authentication guard that redirects unauthenticated users to /login.
5. THE PWA_App SHALL redirect users to the appropriate default route based on their profile interface: /dashboard for central profiles and /helpdesk for helpdesk profiles.

---

### Requirement 21: Containerization and Deployment

**User Story:** As a DevOps engineer, I want the frontend to be containerized and served via a lightweight web server, so that it integrates with the existing Docker Compose stack.

#### Acceptance Criteria

1. THE PWA_App SHALL provide a multi-stage Dockerfile: build stage (Node.js for npm build) and runtime stage (Nginx Alpine for serving static files).
2. THE PWA_App SHALL be added to the existing docker-compose.yml as a new service named "frontend" with a configurable host port (default: 3000).
3. THE Nginx configuration SHALL proxy API requests (paths starting with /api/) to the API_Gateway service, enabling the frontend to communicate with the backend without CORS issues in production.
4. THE PWA_App SHALL support environment variable injection at build time for: API_GATEWAY_URL, APP_VERSION, and PUBLIC_URL.
5. THE Docker image SHALL run as a non-root user, matching the security pattern of the backend microservice images.
6. THE PWA_App SHALL be buildable with a single command: `docker compose build frontend`.

---

### Requirement 22: Accessibility

**User Story:** As a user with accessibility needs, I want the frontend to follow accessibility best practices, so that I can use the application with assistive technologies.

#### Acceptance Criteria

1. THE PWA_App SHALL use semantic HTML5 elements (nav, main, aside, header, footer, article, section) for the page structure.
2. THE PWA_App SHALL provide ARIA labels on all interactive elements that lack visible text labels (icon buttons, status badges, action menus).
3. THE PWA_App SHALL support keyboard navigation for all interactive elements, including: tab order through form fields, Enter/Space to activate buttons, Escape to close modals and dropdowns.
4. THE PWA_App SHALL maintain a minimum color contrast ratio of 4.5:1 for normal text and 3:1 for large text against their backgrounds in the default theme.
5. THE PWA_App SHALL announce dynamic content changes (toast notifications, form validation errors, loading states) to screen readers using ARIA live regions.
6. THE PWA_App SHALL provide visible focus indicators on all focusable elements.

---

### Requirement 23: Internationalization

**User Story:** As a user, I want the frontend to support multiple languages, so that I can use the application in my preferred language.

#### Acceptance Criteria

1. THE PWA_App SHALL implement an internationalization (i18n) framework that externalizes all user-facing strings into locale files.
2. THE PWA_App SHALL ship with English (en-US) as the default and only locale for the MVP.
3. THE i18n framework SHALL support locale switching at runtime without a full page reload.
4. THE PWA_App SHALL format dates, numbers, and currencies according to the active locale using the Intl API.
5. THE PWA_App SHALL support right-to-left (RTL) text direction for future locale additions by using logical CSS properties (margin-inline-start, padding-inline-end) instead of physical properties.

---

### Requirement 24: Error Handling and Notifications

**User Story:** As a user, I want clear feedback when actions succeed or fail, so that I know the current state of my operations.

#### Acceptance Criteria

1. WHEN an API request succeeds for a create, update, or delete operation, THE PWA_App SHALL display a success toast notification with a brief description of the completed action.
2. WHEN an API request fails, THE PWA_App SHALL display an error toast notification with the error message from the API response body.
3. THE PWA_App SHALL display toast notifications in the top-right corner of the viewport, auto-dismissing after 5 seconds, matching the legacy GLPI toast notification pattern.
4. WHEN a form submission fails with HTTP 422 validation errors, THE PWA_App SHALL display inline error messages next to each invalid field and a summary error banner at the top of the form.
5. THE PWA_App SHALL display a full-page error boundary with a "Something went wrong" message and a retry button when an unhandled JavaScript error occurs.
6. IF the API_Gateway is unreachable, THEN THE PWA_App SHALL display a persistent connection error banner at the top of the page until connectivity is restored.

---

## Summary of Requirements

| # | Requirement | Scope | Type |
|---|---|---|---|
| 1 | Login and Session Management | Authentication | Functional |
| 2 | Profile and Entity Switching | Authentication | Functional |
| 3 | Central Interface Layout | Navigation | Functional |
| 4 | Helpdesk Interface Layout | Navigation | Functional |
| 5 | Ticket List View | Ticket Management | Functional |
| 6 | Ticket Detail View | Ticket Management | Functional |
| 7 | Ticket Timeline Actions | Ticket Management | Functional |
| 8 | Ticket Creation | Ticket Management | Functional |
| 9 | Problem List and Detail Views | Problem Management | Functional |
| 10 | Change List and Detail Views | Change Management | Functional |
| 11 | Asset List and Detail Views | Asset/CMDB | Functional |
| 12 | Software and License Views | Asset/CMDB | Functional |
| 13 | Knowledge Base Browsing | Knowledge Base | Functional |
| 14 | Global Search | Search | Functional |
| 15 | Dashboard | Dashboard | Functional |
| 16 | User Preferences | User Settings | Functional |
| 17 | Progressive Web App Capabilities | PWA | Non-Functional |
| 18 | Responsive Design | UI/UX | Non-Functional |
| 19 | API Integration — HTTP Client | Infrastructure | Technical |
| 20 | Routing and Code Splitting | Infrastructure | Technical |
| 21 | Containerization and Deployment | DevOps | Non-Functional |
| 22 | Accessibility | UI/UX | Non-Functional |
| 23 | Internationalization | UI/UX | Non-Functional |
| 24 | Error Handling and Notifications | UI/UX | Functional |
