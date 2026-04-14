# Implementation Plan: GLPI PWA Frontend MVP

## Overview

This plan implements the GLPI PWA Frontend as a React 18 + TypeScript + Vite application consuming the GLPI Microservices Backend. Tasks are ordered for incremental development: project scaffolding and infrastructure first, then shared components, then feature modules, and finally PWA/containerization. Each task builds on previous tasks with no orphaned code.

## Tasks

- [x] 1. Scaffold project structure, tooling, and core configuration
  - Initialize Vite + React + TypeScript project under `frontend/`
  - Install dependencies: react, react-dom, react-router-dom, zustand, @tanstack/react-query, axios, tiptap, recharts, react-i18next, i18next, workbox, fast-check, vitest, @testing-library/react
  - Create `tsconfig.json`, `vite.config.ts`, `vitest.config.ts` with jsdom environment
  - Create project directory structure: `src/api/`, `src/stores/`, `src/hooks/`, `src/layouts/`, `src/components/`, `src/pages/`, `src/theme/`, `src/i18n/`, `src/utils/`, `src/__tests__/`
  - Create `src/main.tsx` entry point and `src/App.tsx` root component with provider wrappers (QueryClientProvider, BrowserRouter)
  - Create `src/i18n/index.ts` with react-i18next configuration and `src/i18n/locales/en-US.json` with initial empty namespace
  - Create `src/test-setup.ts` for Vitest global setup
  - _Requirements: 20.1, 20.2, 23.1, 23.2_

- [ ] 2. Implement design token system, theme engine, and CSS foundation
  - [x] 2.1 Create base design tokens and palette CSS files
    - Create `src/theme/tokens.css` with all CSS custom properties (sidebar colors, primary accent, body bg, timeline colors, topbar height)
    - Create palette CSS files under `src/theme/palettes/` for all 18 legacy palettes (default, classic, dark, darker, midnight, auror, teclib, etc.) using `[data-glpi-theme="{name}"]` selectors
    - Dark palettes must additionally set `[data-glpi-theme-dark="1"]`
    - _Requirements: 3.6, 3.7, 3.8, 16.3, 16.5_

  - [x] 2.2 Implement theme engine runtime switching
    - Create `src/theme/themeEngine.ts` with `applyTheme(themeName)` function that sets `data-glpi-theme` and `data-glpi-theme-dark` attributes on document root
    - Define `DARK_PALETTES` constant array and `SUPPORTED_PALETTES` list
    - _Requirements: 16.3_

  - [ ]* 2.3 Write property test for theme palette application (Property 11)
    - **Property 11: Theme palette application**
    - For any valid palette name from the 18 supported palettes, `applyTheme` must set `data-glpi-theme` correctly and set `data-glpi-theme-dark` to `"1"` for dark palettes
    - **Validates: Requirements 16.3**

- [ ] 3. Implement HTTP client, auth store, and token management
  - [x] 3.1 Create Axios HTTP client with interceptors
    - Create `src/api/client.ts` with Axios instance: base URL `/api`, request interceptor to attach JWT Bearer header, response interceptor for 401 refresh flow (queue concurrent requests), 429 rate limit handling with Retry-After, network error detection
    - Create `src/api/endpoints.ts` with API endpoint constants for all services
    - Create `src/api/types.ts` with `ApiResponse<T>`, `PaginationMeta`, `ApiError` interfaces
    - _Requirements: 19.1, 19.2, 19.3, 19.4, 19.5, 19.6_

  - [x] 3.2 Implement auth store with Zustand
    - Create `src/stores/authStore.ts` with `AuthState` interface: accessToken, refreshToken, user (UserContext), isAuthenticated, isLoading, rememberMe
    - Implement `login`, `loginWith2FA`, `logout`, `refreshAccessToken`, `switchProfile`, `switchEntity` actions
    - Token storage: access token in memory only, refresh token in localStorage when rememberMe is checked
    - _Requirements: 1.2, 1.3, 1.4, 1.5, 1.9, 2.3, 2.5_

  - [ ]* 3.3 Write property test for JWT Authorization header attachment (Property 1)
    - **Property 1: JWT Authorization header attachment**
    - For any API request URL and method, the interceptor must attach `Authorization: Bearer {token}` when a token exists, and omit the header when no token is stored
    - **Validates: Requirements 1.6, 19.1**

- [ ] 4. Implement utility functions (status, priority, pagination, formatters, validators)
  - [x] 4.1 Create status and priority utility modules
    - Create `src/utils/status.ts` with `STATUS_CONFIG` map (6 statuses with label, color, icon), `getStatusConfig(code)`, `getTimelineEntryColors(type)` for 7 entry types, `getSlaIndicatorColor(deadline, current, totalDuration)`
    - Create `src/utils/priority.ts` with `PRIORITY_CONFIG` map (6 priorities with label, color), `getPriorityConfig(code)`, and priority matrix computation
    - _Requirements: 5.6, 5.7, 6.2, 6.8_

  - [x] 4.2 Create pagination and search utility modules
    - Create `src/utils/pagination.ts` with `computePaginationMeta(totalElements, pageSize, currentPage)` and `truncateSearchResults(groupedResults, maxPerCategory)` functions
    - _Requirements: 5.2, 14.4, 19.4_

  - [x] 4.3 Create formatters and validators utility modules
    - Create `src/utils/formatters.ts` with `formatDate(date, locale)` and `formatNumber(number, locale)` using Intl API
    - Create `src/utils/validators.ts` with `mapValidationErrors(errors)` that converts API error array to `Record<string, string[]>`
    - _Requirements: 8.5, 23.4, 24.4_

  - [ ]* 4.4 Write property tests for status and priority config (Properties 4, 5)
    - **Property 4: Status display config completeness** — for any status code in [1..6], `STATUS_CONFIG` returns non-null entry with non-empty label, valid hex color, non-empty icon
    - **Property 5: Priority display config completeness** — for any priority code in [1..6], `PRIORITY_CONFIG` returns non-null entry with non-empty label, valid hex color
    - **Validates: Requirements 5.6, 5.7**

  - [ ]* 4.5 Write property tests for timeline colors and SLA indicator (Properties 6, 7)
    - **Property 6: Timeline entry color resolution** — for any entry type in the set, the resolver returns non-null color config with valid CSS color strings
    - **Property 7: SLA deadline indicator color** — returns green (>25% remaining), orange (<=25% remaining), red (deadline passed)
    - **Validates: Requirements 6.2, 6.8**

  - [ ]* 4.6 Write property tests for pagination, search truncation, validators, and formatters (Properties 3, 8, 10, 13)
    - **Property 3: Pagination metadata consistency** — totalPages = ceil(totalElements / pageSize), currentPage in valid range
    - **Property 8: Validation error field mapping** — every input error field appears as key, no fields lost or duplicated
    - **Property 10: Search results category truncation** — at most 5 results per category, hasMore flag when count > 5
    - **Property 13: Date and number formatting locale consistency** — output matches Intl API
    - **Validates: Requirements 5.2, 8.5, 14.4, 19.4, 23.4, 24.4**

- [x] 5. Checkpoint — Ensure all infrastructure tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 6. Implement preferences store, UI store, and routing with auth guard
  - [x] 6.1 Create preferences and UI stores
    - Create `src/stores/preferencesStore.ts` with theme, locale, layoutMode, timelineOrder, itemsPerPage, sidebarCollapsed — persisted to localStorage
    - Create `src/stores/uiStore.ts` for transient UI state (modals, sidebar toggle)
    - _Requirements: 16.1, 16.2, 16.4_

  - [x] 6.2 Implement router with lazy loading and auth guard
    - Create `src/router.tsx` with all routes from the routing map, using `React.lazy` and `Suspense` for code splitting
    - Implement `RequireAuth` wrapper that checks `authStore.isAuthenticated` and redirects to `/login` with `returnTo` param
    - Implement `isHelpdeskRouteAllowed` function for helpdesk profile route restriction
    - Implement profile-based redirect: `/dashboard` for central, `/helpdesk` for helpdesk
    - Create `src/components/common/LoadingSkeleton.tsx` for Suspense fallback
    - _Requirements: 20.1, 20.2, 20.3, 20.4, 20.5, 4.5_

  - [ ]* 6.3 Write property tests for auth guard and helpdesk route restriction (Properties 2, 12)
    - **Property 2: Helpdesk route restriction** — non-allowed routes redirect to `/helpdesk` when profile is helpdesk
    - **Property 12: Auth guard redirect** — unauthenticated access to any non-login route redirects to `/login` with returnTo param
    - **Validates: Requirements 4.5, 20.4**

  - [ ]* 6.4 Write property test for license compliance color coding (Property 9)
    - **Property 9: License compliance color coding** — green when usage < 80%, orange when 80-100%, red when over-licensed
    - Create `getLicenseComplianceColor` in `src/utils/status.ts`
    - **Validates: Requirements 12.3**

- [ ] 7. Implement shared UI components (common and navigation)
  - [x] 7.1 Create common shared components
    - Create `src/components/common/StatusBadge.tsx` — colored badge using STATUS_CONFIG
    - Create `src/components/common/PriorityBadge.tsx` — colored indicator using PRIORITY_CONFIG
    - Create `src/components/common/ActorBadge.tsx` — user avatar/initials badge
    - Create `src/components/common/Toast.tsx` — notification toast system (top-right, auto-dismiss 5s, success/error/warning/info, ARIA live regions)
    - Create `src/components/common/ErrorBoundary.tsx` — catch-all error UI with retry button
    - Create `src/components/common/RichTextEditor.tsx` — TipTap wrapper with bold, italic, underline, lists, links, code blocks, file attachments
    - Create `src/components/common/Pagination.tsx` — page controls with configurable page size
    - _Requirements: 5.6, 5.7, 6.7, 7.6, 22.1, 22.2, 22.5, 24.1, 24.2, 24.3, 24.5_

  - [x] 7.2 Create SearchEngine reusable data table component
    - Create `src/components/common/SearchEngine.tsx` with configurable columns, server-side pagination via TanStack Query, column sorting, filter controls, checkbox selection for bulk actions, horizontal scroll on narrow viewports, pagination footer
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.8, 18.5_

  - [x] 7.3 Create navigation components
    - Create `src/components/navigation/Sidebar.tsx` — vertical nav with GLPI logo, collapsible menu sections (Assets, Assistance, Management, Tools, Administration with "coming soon"), collapsed mode (icons only), sidebar colors matching legacy
    - Create `src/components/navigation/TopBar.tsx` — breadcrumbs, global search input, user menu
    - Create `src/components/navigation/Breadcrumbs.tsx` — route-based breadcrumb trail
    - Create `src/components/navigation/UserMenu.tsx` — profile/entity switcher dropdown
    - Create `src/components/navigation/GlobalSearch.tsx` — search dropdown with categorized results, 300ms debounce, max 5 per category, "View all" link
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 14.1, 14.2, 14.3, 14.4, 14.5, 22.3_

  - [ ]* 7.4 Write unit tests for shared components
    - Test StatusBadge, PriorityBadge, Toast, ErrorBoundary rendering and behavior
    - Test SearchEngine pagination, sorting, and filter interactions
    - Test Sidebar collapse/expand, TopBar rendering, GlobalSearch debounce
    - _Requirements: 5.6, 5.7, 24.1, 24.5_

- [x] 8. Implement layouts (Central, Helpdesk, Auth)
  - Create `src/layouts/CentralLayout.tsx` — Sidebar (left, 15rem expanded / 70px collapsed) + TopBar (top) + content area (right) with light background
  - Create `src/layouts/HelpdeskLayout.tsx` — horizontal top nav, no sidebar
  - Create `src/layouts/AuthLayout.tsx` — centered card with GLPI logo matching legacy login card
  - Wire layouts into router: AuthLayout for `/login`, CentralLayout for central profile routes, HelpdeskLayout for helpdesk profile routes
  - Implement responsive behavior: sidebar collapses to hamburger below 992px, content stacks below 768px
  - _Requirements: 3.1, 3.4, 3.5, 3.8, 4.1, 18.1, 18.2, 18.6, 22.1_

- [-] 9. Implement authentication pages (Login, 2FA)
  - Create `src/pages/LoginPage.tsx` with username/password fields, "Remember me" checkbox, sign-in button, matching legacy login card layout
  - Implement login form submission calling `authStore.login()`, handle HTTP 401 (invalid credentials), HTTP 401 with account locked error message, HTTP 403 with 2FA_REQUIRED code
  - Create TOTP code input field for 2FA flow calling `authStore.loginWith2FA()`
  - Implement post-login redirect based on profile interface (central → `/dashboard`, helpdesk → `/helpdesk`)
  - Handle `returnTo` query parameter for redirect after login
  - _Requirements: 1.1, 1.2, 1.4, 1.5, 1.7, 1.8, 1.9, 1.10, 20.5_

- [~] 10. Checkpoint — Ensure all tests pass, verify login flow and layout rendering
  - Ensure all tests pass, ask the user if questions arise.

- [~] 11. Implement TanStack Query hooks for all API modules
  - Create `src/hooks/useAuth.ts` — auth operations hook wrapping authStore actions
  - Create `src/hooks/useTickets.ts` — list (paginated, filtered, sorted), detail, create, update, add followup/task/solution
  - Create `src/hooks/useProblems.ts` — list, detail, create, update, add followup/task/solution
  - Create `src/hooks/useChanges.ts` — list, detail, create, update, add followup/task/solution, validation actions
  - Create `src/hooks/useAssets.ts` — list (by type), detail (with tabs data), software list, license list
  - Create `src/hooks/useKnowledge.ts` — list, detail, search, view counter increment
  - Create `src/hooks/useSearch.ts` — global search with debounce and categorized results
  - Create `src/hooks/useDashboard.ts` — dashboard widget data (counters, charts, activity feed)
  - _Requirements: 5.1, 6.1, 7.5, 8.4, 9.1, 10.1, 11.1, 12.1, 13.1, 14.1, 15.2_

- [~] 12. Implement Dashboard page
  - Create `src/pages/DashboardPage.tsx` as default landing page for Central_Interface
  - Create `src/components/dashboard/CounterWidget.tsx` — clickable counter cards (open tickets, my assigned, overdue)
  - Create `src/components/dashboard/BarChartWidget.tsx` — tickets by status bar chart using Recharts
  - Create `src/components/dashboard/PieChartWidget.tsx` — tickets by priority pie chart using Recharts
  - Implement responsive grid layout for widgets
  - Counter clicks navigate to filtered list views (e.g., overdue → `/tickets?status=overdue`)
  - _Requirements: 15.1, 15.2, 15.3, 15.4, 15.5_

- [ ] 13. Implement ITIL shared components (Timeline, FieldsPanel, forms)
  - [~] 13.1 Create Timeline and TimelineEntry components
    - Create `src/components/itil/Timeline.tsx` — chronological entry list with configurable order (newest/oldest first)
    - Create `src/components/itil/TimelineEntry.tsx` — single entry with colored left border per type (followup=#ececec, task=#ffe8b9, solution=#9fd6ed, document=#80cead)
    - Render action buttons at bottom: Add Followup, Add Task, Add Solution, Add Document
    - Display approve/reject buttons on pending solution entries for users with validation rights
    - _Requirements: 6.2, 7.1, 7.7_

  - [~] 13.2 Create FieldsPanel component
    - Create `src/components/itil/FieldsPanel.tsx` — collapsible right-side panel with accordion sections: Status, Dates, Actors, Priority/Urgency/Impact, Category, SLA, Linked Items
    - Implement collapsed mode (90px icon strip) and expanded mode (4/12 width)
    - Field updates send PATCH requests to API
    - SLA deadline indicators: green (on track), orange (approaching), red (breached)
    - _Requirements: 6.3, 6.4, 6.5, 6.7, 6.8_

  - [~] 13.3 Create timeline action forms
    - Create `src/components/itil/FollowupForm.tsx` — Rich_Text_Editor + private/public toggle
    - Create `src/components/itil/TaskForm.tsx` — Rich_Text_Editor, assigned user selector, date pickers, duration, status (TODO/DONE)
    - Create `src/components/itil/SolutionForm.tsx` — Rich_Text_Editor + solution type selector
    - Create `src/components/itil/ActorSelector.tsx` — user/group/supplier search and selection
    - _Requirements: 7.2, 7.3, 7.4, 7.6_

- [ ] 14. Implement Ticket module (list, detail, create)
  - [~] 14.1 Create TicketListPage
    - Create `src/pages/tickets/TicketListPage.tsx` using SearchEngine with columns: ID, status icon, title, requester, assigned, priority, last update, entity
    - Implement filters: status, priority, entity, assigned user, category
    - Row click navigates to `/tickets/{id}`
    - Bulk actions: change status, assign, delete via checkboxes and toolbar
    - _Requirements: 5.1, 5.2, 5.3, 5.4, 5.5, 5.6, 5.7, 5.8_

  - [~] 14.2 Create TicketDetailPage
    - Create `src/pages/tickets/TicketDetailPage.tsx` with two-column layout: Timeline (8/12) + FieldsPanel (4/12)
    - Display ticket title and ID in header
    - Wire Timeline actions (followup, task, solution) to API hooks
    - Wire FieldsPanel field updates to PATCH API
    - Responsive: stack to single column below 768px
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8, 7.1, 7.2, 7.3, 7.4, 7.5, 7.7, 18.3_

  - [~] 14.3 Create TicketCreatePage
    - Create `src/pages/tickets/TicketCreatePage.tsx` with form: type (Incident/Request), title, content (RichTextEditor), category (filtered by type), urgency (filtered by entity mask), entity, actors
    - Auto-populate requester with current user
    - On submit POST to `/tickets`, navigate to detail on success
    - Display inline validation errors on HTTP 422
    - Simplified form for helpdesk interface (title, content, urgency, category only)
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5, 8.6, 8.7_

- [~] 15. Implement Problem module (list, detail, create)
  - Create `src/pages/problems/ProblemListPage.tsx` using SearchEngine with columns: ID, status, title, priority, assigned, entity, linked tickets count, last update
  - Create `src/pages/problems/ProblemDetailPage.tsx` with two-column layout, additional FieldsPanel sections for impact analysis, root cause, symptom description (each with RichTextEditor)
  - Display linked tickets with clickable navigation
  - Create `src/pages/problems/ProblemCreatePage.tsx` with fields: title, content, urgency, impact, category, actors, linked tickets
  - Wire timeline actions (followup, task, solution) reusing ITIL shared components
  - _Requirements: 9.1, 9.2, 9.3, 9.4, 9.5, 9.6_

- [~] 16. Implement Change module (list, detail, create)
  - Create `src/pages/changes/ChangeListPage.tsx` using SearchEngine with columns: ID, status, title, priority, assigned, entity, last update
  - Create `src/pages/changes/ChangeDetailPage.tsx` with two-column layout, additional FieldsPanel sections for planning documents (impact, control list, rollout plan, backout plan, checklist), validation workflow display with approve/reject buttons
  - Display linked tickets and problems with clickable navigation
  - Create `src/pages/changes/ChangeCreatePage.tsx` with fields: title, content, urgency, impact, category, actors, linked tickets/problems
  - Wire timeline actions reusing ITIL shared components
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5, 10.6, 10.7, 10.8_

- [~] 17. Checkpoint — Ensure all ITIL module tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 18. Implement Asset/CMDB module (assets, software, licenses)
  - [~] 18.1 Create AssetListPage and AssetDetailPage
    - Create `src/pages/assets/AssetListPage.tsx` with type filter tab bar (All, Computers, Network Equipment, Monitors, Printers, Phones, Peripherals, Software) and SearchEngine columns: ID, name, type, status, location, assigned user, entity, serial, last update
    - Create `src/pages/assets/AssetDetailPage.tsx` with tabbed sections: General Information, Components (computers), Software (computers), Network Ports, Financial Information, Contracts, Linked Tickets, History
    - Navigate from asset to linked tickets/problems/changes
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.6, 11.7, 11.8_

  - [~] 18.2 Create SoftwareListPage and LicenseListPage
    - Create `src/pages/assets/SoftwareListPage.tsx` with columns: name, manufacturer, category, installations count, licenses count
    - Create `src/pages/assets/LicenseListPage.tsx` with columns: name, software, license type, serial, total seats, used seats, remaining seats, expiry date
    - License compliance color indicator: green (available), orange (>=80% used), red (over-licensed/expired)
    - Software detail page: general info, version list, installations per version
    - _Requirements: 12.1, 12.2, 12.3, 12.4_

- [~] 19. Implement Knowledge Base module
  - Create `src/pages/knowledge/KnowledgeListPage.tsx` with category tree (left) and article list (right), full-text search input, FAQ badge indicator
  - Create `src/pages/knowledge/KnowledgeDetailPage.tsx` displaying: title, rendered rich text content, author, dates, view count, linked items
  - Increment view counter on article load via API
  - Helpdesk interface shows only FAQ articles visible to user's entity/profile
  - Navigate from article to linked tickets/problems/changes
  - _Requirements: 13.1, 13.2, 13.3, 13.4, 13.5, 13.6, 13.7_

- [ ] 20. Implement Helpdesk home page and User Preferences page
  - [~] 20.1 Create HelpdeskHomePage
    - Create `src/pages/HelpdeskHomePage.tsx` with search banner, tile cards (Create a Ticket, My Tickets, FAQ), tabbed section showing open and recently solved tickets
    - _Requirements: 4.2, 4.3, 4.4_

  - [~] 20.2 Create PreferencesPage
    - Create `src/pages/PreferencesPage.tsx` with settings: display language, theme/palette selector (live preview), layout mode, timeline order, items per page, sidebar collapsed state
    - Theme change applies immediately via `applyTheme()` updating CSS custom properties
    - Persist preferences locally and sync with backend when available
    - _Requirements: 16.1, 16.2, 16.3, 16.4, 16.5_

- [~] 21. Implement Profile/Entity switching in UserMenu
  - Implement profile selector dropdown in UserMenu fetching all user profiles from API
  - Implement entity selector with entity tree hierarchy display
  - Profile/entity switch obtains new JWT and reloads current view
  - Display current profile name and entity name in TopBar matching legacy layout
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 2.5, 2.6_

- [~] 22. Checkpoint — Ensure all feature module tests pass
  - Ensure all tests pass, ask the user if questions arise.

- [~] 23. Implement PWA capabilities (service worker, manifest, offline support)
  - Create `public/manifest.json` with app name "GLPI", short name "GLPI", start URL, display "standalone", theme color #2f3f64, background color #f5f7fb, icons (192x192, 512x512)
  - Configure Workbox via vite-plugin-pwa: CacheFirst for app shell and static assets, NetworkFirst for API requests, StaleWhileRevalidate for images
  - Implement offline indicator banner when `navigator.onLine` is false
  - Implement service worker update notification banner with refresh prompt
  - Create PWA icons in `public/icons/`
  - _Requirements: 17.1, 17.2, 17.3, 17.4, 17.5, 17.6_

- [~] 24. Implement responsive design refinements and accessibility audit
  - Ensure touch-friendly targets (min 44x44px) on mobile viewports
  - Verify semantic HTML5 elements across all layouts and pages (nav, main, aside, header, footer, article, section)
  - Add ARIA labels on all icon buttons, status badges, and action menus
  - Implement keyboard navigation: tab order, Enter/Space activation, Escape to close modals/dropdowns
  - Add visible focus indicators on all focusable elements
  - Verify 4.5:1 contrast ratio for normal text, 3:1 for large text in default theme
  - Ensure RTL-ready CSS using logical properties (margin-inline-start, padding-inline-end)
  - Hide non-essential UI on mobile (breadcrumbs, secondary toolbars)
  - _Requirements: 18.1, 18.3, 18.4, 18.5, 18.6, 22.1, 22.2, 22.3, 22.4, 22.5, 22.6, 23.5_

- [~] 25. Implement containerization and Docker Compose integration
  - Create `frontend/Dockerfile` with multi-stage build: Node.js build stage (`npm run build`) + Nginx Alpine runtime stage
  - Create `frontend/nginx/default.conf` with `/api/` proxy pass to API Gateway service and SPA fallback
  - Docker image runs as non-root user
  - Support build-time environment variables: `API_GATEWAY_URL`, `APP_VERSION`, `PUBLIC_URL`
  - Add `frontend` service to existing `docker-compose.yml` with configurable host port (default: 3000)
  - Update `.gitignore` and `.dockerignore` for frontend artifacts (node_modules, dist, coverage)
  - Verify buildable with `docker compose build frontend`
  - _Requirements: 21.1, 21.2, 21.3, 21.4, 21.5, 21.6_

- [~] 26. Final checkpoint — Ensure all tests pass and application is fully integrated
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 27. Version control and release
  - [~] 27.1 Ensure all previous tasks are complete and tests pass
  - [~] 27.2 Remove SNAPSHOT suffix from all version references in the codebase
  - [~] 27.3 Commit the version bump: "release: 1.1.0 - glpi-pwa-frontend"
  - [~] 27.4 Merge branch into main/master
  - [~] 27.5 Apply Git tag: 1.1.0 (without SNAPSHOT)
  - [~] 27.6 Push branch, merge, and tag to remote

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation at key milestones
- Property tests validate the 13 universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- All code is TypeScript, all strings in en-US
- The `frontend/` directory is the project root for the React application
