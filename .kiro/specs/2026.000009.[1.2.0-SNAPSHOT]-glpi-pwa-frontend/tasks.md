# Implementation Plan: GLPI PWA Frontend

## Overview

Build the GLPI PWA Frontend MVP by copying and adapting the TailAdmin React v2.1.0 template (`.frontend-template/`, read-only) into `frontend/`. The implementation covers project scaffolding, JWT authentication (login, token management, logout), a dashboard landing page, error pages (404/500/503), reusable alert components, PWA installability via `vite-plugin-pwa`, and Docker deployment with Nginx reverse proxy. All code is TypeScript + React 19 + Tailwind CSS v4 + Vite.

## Tasks

- [x] 1. Scaffold project structure from template
  - [x] 1.1 Copy template foundation into `frontend/`
    - Copy `index.html`, `package.json`, `tsconfig.json`, `tsconfig.app.json`, `tsconfig.node.json`, `postcss.config.js`, `eslint.config.js`, `vite.config.ts`, and `.gitignore` from `.frontend-template/` into `frontend/`
    - Copy `src/main.tsx` entry point from template
    - Copy `src/context/ThemeContext.tsx` and `src/context/SidebarContext.tsx` from template
    - Copy `src/hooks/useGoBack.ts` from template
    - Copy `src/components/common/PageMeta.tsx`, `GridShape.tsx`, `ThemeToggleButton.tsx`, `ThemeTogglerTwo.tsx`, `ScrollToTop.tsx` from template
    - Copy `src/components/ui/button/Button.tsx` from template
    - Copy `src/components/ui/alert/Alert.tsx` from template
    - Copy `src/layout/AppLayout.tsx`, `AppHeader.tsx`, `AppSidebar.tsx`, `Backdrop.tsx` from template
    - Copy `src/pages/AuthPages/AuthPageLayout.tsx`, `SignIn.tsx` from template
    - Copy `src/pages/Dashboard/Home.tsx` from template
    - Copy `src/pages/OtherPage/NotFound.tsx` from template (rename to `src/pages/ErrorPages/NotFound.tsx`)
    - Copy `src/components/auth/SignInForm.tsx` from template
    - Copy `src/components/header/UserDropdown.tsx` from template
    - Copy `public/favicon.png`, `public/images/logo/`, `public/images/error/` from template
    - Copy required SVG icon files from `src/icons/` (only those referenced by copied components)
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 1.2 Update `package.json` and install dependencies
    - Update `name` to `glpi-frontend`, `version` to `1.2.0-SNAPSHOT`
    - Remove unused template dependencies: `@fullcalendar/*`, `@react-jvectormap/*`, `apexcharts`, `react-apexcharts`, `react-dnd`, `react-dnd-html5-backend`, `react-dropzone`, `flatpickr`, `swiper`
    - Add new dependencies: `axios`
    - Add new dev dependencies: `vite-plugin-pwa`, `vitest`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, `fast-check`, `msw`, `jsdom`
    - Add `test` script: `"test": "vitest --run"`
    - Run `npm install` to generate `package-lock.json`
    - _Requirements: 1.2, 1.3_

  - [x] 1.3 Strip unused template code and create MVP file structure
    - Remove all pages not in MVP scope (Charts, Forms, Tables, UiElements, Calendar, Blank, UserProfiles, SignUp)
    - Remove unused components (charts, ecommerce, form, tables, UserProfile, avatar, badge, dropdown, images, modal, table, videos, ChartTab, ComponentCard, PageBreadCrumb, NotificationDropdown, SidebarWidget)
    - Remove unused SVG icons not referenced by MVP components
    - Create empty placeholder directories: `src/services/`, `src/types/`, `src/components/routes/`
    - Create `frontend/.env.example` with `VITE_API_GATEWAY_URL=/api` and `VITE_APP_VERSION=1.2.0-SNAPSHOT`
    - _Requirements: 1.3, 1.4, 10.4_

- [x] 2. Checkpoint — Verify scaffolding
  - Ensure the project compiles with `npm run build` (no TypeScript errors)
  - Ensure all tests pass, ask the user if questions arise

- [x] 3. Implement core infrastructure (types, API client, auth service)
  - [x] 3.1 Create TypeScript auth types
    - Create `src/types/auth.ts` with `LoginRequest`, `AuthResponse`, `RefreshRequest`, and `ApiError` interfaces as defined in the design document
    - _Requirements: 3.3, 4.1, 8.1_

  - [x] 3.2 Implement Axios HTTP client with interceptors
    - Create `src/services/api.ts` with an Axios instance
    - Set base URL from `import.meta.env.VITE_API_GATEWAY_URL || '/api'`
    - Implement request interceptor that attaches `Authorization: Bearer {accessToken}` header when a token is available
    - Implement response interceptor that on 401 for non-auth endpoints (`/auth/*`) attempts one token refresh and retries the original request; if retry fails, clears session and redirects to `/signin`
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

  - [x] 3.3 Implement auth service functions
    - Create `src/services/authService.ts` with `login()`, `refresh()`, and `logout()` functions
    - `login()` sends POST to `/auth/login` with `LoginRequest` payload
    - `refresh()` sends POST to `/auth/refresh` with `{ refreshToken }`
    - `logout()` sends POST to `/auth/logout` with Bearer token header
    - _Requirements: 3.3, 4.2, 5.2_

  - [ ]* 3.4 Write property test: Login payload construction (Property 2)
    - **Property 2: Login payload construction**
    - Generate random username, password strings and optional totpCode numbers; verify the login service produces a POST payload where every provided field matches input exactly and totpCode is omitted when not provided
    - **Validates: Requirements 3.3**

  - [ ]* 3.5 Write property test: Server error classification (Property 3)
    - **Property 3: Server error classification**
    - Generate random HTTP status codes in the 5xx range (500–599) and network errors; verify the error classification logic categorizes them as "server" error type, distinct from 401 or 429
    - **Validates: Requirements 3.7**

  - [ ]* 3.6 Write property test: Authorization header formatting (Property 6)
    - **Property 6: Authorization header formatting**
    - Generate random non-null access token strings; verify the request interceptor produces an `Authorization` header with value `Bearer {accessToken}` with no extra whitespace or modification
    - **Validates: Requirements 4.5, 8.2**

  - [ ]* 3.7 Write property test: 401 interceptor retry for non-auth endpoints (Property 7)
    - **Property 7: 401 interceptor retry for non-auth endpoints**
    - Generate random API request URLs; verify that non-auth URLs trigger exactly one refresh + retry on 401, while auth endpoints (`/auth/login`, `/auth/refresh`, `/auth/logout`) never trigger retry logic
    - **Validates: Requirements 8.3**

- [x] 4. Implement AuthContext and token management
  - [x] 4.1 Create AuthContext provider
    - Create `src/context/AuthContext.tsx` implementing `AuthState` and `AuthContextType` interfaces from the design
    - On mount, read tokens from `localStorage` keys (`glpi_access_token`, `glpi_refresh_token`, `glpi_expires_at`); if a valid non-expired access token exists, restore the session and set `isAuthenticated = true`
    - Implement `login()` method that calls `authService.login()`, stores tokens in memory + localStorage, and schedules refresh timer
    - Implement `logout()` method that calls `authService.logout()`, clears all tokens from memory + localStorage regardless of API response, and redirects to `/signin`
    - Implement `getAccessToken()` method for the HTTP client interceptor
    - Set `isLoading = true` during initial session restore, `false` once complete
    - _Requirements: 4.1, 4.3, 4.4, 4.6, 5.2, 5.3_

  - [x] 4.2 Implement automatic token refresh timer
    - Schedule a refresh timer that fires 60 seconds before `expiresAt`
    - On refresh success, replace stored tokens in memory + localStorage and reschedule timer
    - On refresh failure (401), clear all tokens and set `isAuthenticated = false` (triggers redirect via ProtectedRoute)
    - Clean up timer on unmount
    - _Requirements: 4.2, 4.3, 4.4_

  - [x] 4.3 Wire AuthContext into the Axios HTTP client
    - Connect the Axios request interceptor to read the access token from AuthContext via `getAccessToken()`
    - Connect the 401 response interceptor to call the refresh logic from AuthContext
    - _Requirements: 4.5, 8.2, 8.3, 8.4_

  - [ ]* 4.4 Write property test: Token storage round-trip (Property 1)
    - **Property 1: Token storage round-trip**
    - Generate random token sets (accessToken, refreshToken, expiresAt); store them in localStorage and read them back; verify the values are identical to the originals
    - **Validates: Requirements 3.4, 4.1, 4.3, 4.6**

  - [ ]* 4.5 Write property test: Refresh timer scheduling (Property 5)
    - **Property 5: Refresh timer scheduling**
    - Generate random `expiresIn` values greater than 60 seconds; verify the AuthContext schedules a refresh at exactly `(expiresIn − 60)` seconds after token storage
    - **Validates: Requirements 4.2**

- [x] 5. Checkpoint — Verify core infrastructure
  - Ensure all tests pass, ask the user if questions arise

- [x] 6. Implement Login page
  - [x] 6.1 Adapt SignInForm component for GLPI authentication
    - Adapt `src/components/auth/SignInForm.tsx` from the template to include username, password, and optional TOTP code input fields
    - Add client-side validation: reject empty or whitespace-only username and password before sending the request
    - On submit, call `AuthContext.login(username, password, totpCode?)`
    - Display loading indicator and disable submit button while the login request is in progress
    - On success, navigate to the Dashboard (`/`)
    - On 401 error, display an inline Alert (error variant) with "Invalid username or password"
    - On 429 error, display an inline Alert (error variant) with rate-limiting message
    - On 5xx or network error, display an inline Alert (error variant) with server connectivity message
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6, 3.7, 3.8, 3.9, 11.8_

  - [x] 6.2 Adapt SignIn page and AuthPageLayout
    - Adapt `src/pages/AuthPages/SignIn.tsx` to use the modified SignInForm
    - Ensure `AuthPageLayout.tsx` provides the full-page auth layout with theme toggle support
    - If user is already authenticated, redirect to Dashboard (`/`)
    - _Requirements: 6.3_

  - [ ]* 6.3 Write property test: Form validation rejects whitespace-only input (Property 4)
    - **Property 4: Form validation rejects whitespace-only input**
    - Generate strings composed entirely of whitespace characters (spaces, tabs, newlines, empty string); verify the login form validation rejects them as invalid for both username and password fields
    - **Validates: Requirements 3.9**

  - [ ]* 6.4 Write unit tests for Login page
    - Test that the login form renders username, password, TOTP fields and submit button
    - Test that submit button is disabled during loading
    - Test that 401 error displays inline error alert
    - Test that 429 error displays rate-limit alert
    - Test that 5xx error displays server error alert
    - Test that empty fields prevent form submission
    - _Requirements: 3.1, 3.2, 3.5, 3.6, 3.7, 3.8, 3.9_

- [x] 7. Implement route protection and routing
  - [x] 7.1 Create ProtectedRoute component
    - Create `src/components/routes/ProtectedRoute.tsx`
    - Read `isAuthenticated` and `isLoading` from AuthContext
    - While `isLoading`, render a loading spinner
    - If not authenticated, redirect to `/signin`
    - If authenticated, render `<Outlet />`
    - _Requirements: 6.1, 6.2_

  - [x] 7.2 Configure React Router in App.tsx
    - Update `src/App.tsx` to define the routing table:
      - `/signin` → `SignIn` page (AuthPageLayout, no auth required, redirects to `/` if authenticated)
      - `/` → `Home` (Dashboard) wrapped in `ProtectedRoute` inside `AppLayout`
      - `*` → `NotFound` error page (standalone layout)
    - Wrap the app with `AuthProvider` in `main.tsx`
    - Remove all template routes not in MVP scope
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

  - [ ]* 7.3 Write unit tests for ProtectedRoute and routing
    - Test that unauthenticated users are redirected to `/signin`
    - Test that authenticated users can access the Dashboard
    - Test that authenticated users visiting `/signin` are redirected to `/`
    - Test that undefined routes render the 404 page
    - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [x] 8. Implement Dashboard page
  - [x] 8.1 Adapt Dashboard Home page
    - Adapt `src/pages/Dashboard/Home.tsx` to display a welcome message including the authenticated user context
    - Display placeholder widget cards in a responsive grid layout as a foundation for future ITSM dashboard content
    - Ensure the page uses the AppLayout structure (sidebar, header, content area)
    - _Requirements: 7.1, 7.2, 7.3_

  - [x] 8.2 Adapt AppLayout, AppHeader, and AppSidebar for MVP
    - Simplify `AppSidebar.tsx` to show only Dashboard navigation item (remove all template menu items not in MVP)
    - Adapt `AppHeader.tsx` to include the theme toggle and user dropdown with logout
    - Adapt `UserDropdown.tsx` to include a logout control that calls `AuthContext.logout()`
    - Ensure responsive layout: sidebar on desktop, collapsible menu on mobile
    - _Requirements: 5.1, 7.2, 7.4, 7.5_

  - [ ]* 8.3 Write unit tests for Dashboard and layout
    - Test that Dashboard renders welcome message
    - Test that Dashboard renders placeholder widget cards
    - Test that logout control is present in the header
    - Test that theme toggle works (light/dark)
    - _Requirements: 7.1, 7.3, 7.4, 5.1_

- [x] 9. Checkpoint — Verify authentication flow end-to-end
  - Ensure all tests pass, ask the user if questions arise

- [x] 10. Implement error pages and Alert component
  - [x] 10.1 Adapt Alert component for GLPI
    - Adapt `src/components/ui/alert/Alert.tsx` to implement the `AlertProps` interface from the design (variant: success/error/warning/info, title, message, optional action link)
    - Add `role="alert"` ARIA attribute for accessibility
    - Ensure each variant has distinct icon, border color, and background color
    - Support light and dark themes via Tailwind dark classes
    - _Requirements: 11.5, 11.6, 11.7, 11.8, 11.10_

  - [x] 10.2 Implement 404 Not Found error page
    - Adapt `src/pages/ErrorPages/NotFound.tsx` with illustrative SVG image (`404.svg` for light, `404-dark.svg` for dark), error heading, descriptive message, and link to navigate back to Dashboard
    - Render as full-page layout outside AppLayout
    - Add descriptive alt text for error images
    - _Requirements: 11.1, 11.2, 11.9, 11.10_

  - [x] 10.3 Implement 500 Server Error page
    - Create `src/pages/ErrorPages/ServerError.tsx` with illustrative SVG image (`500.svg` / `500-dark.svg`), error heading, descriptive message, and link to navigate back to Dashboard
    - Render as full-page layout outside AppLayout
    - Add descriptive alt text for error images
    - _Requirements: 11.3, 11.9, 11.10_

  - [x] 10.4 Implement 503 Service Unavailable page
    - Create `src/pages/ErrorPages/ServiceUnavailable.tsx` with illustrative SVG image (`503.svg` / `503-dark.svg`), error heading, descriptive message, and a retry action button
    - Render as full-page layout outside AppLayout
    - Add descriptive alt text for error images
    - _Requirements: 11.4, 11.9, 11.10_

  - [ ]* 10.5 Write property test: Alert renders arbitrary content (Property 8)
    - **Property 8: Alert renders arbitrary content**
    - Generate random title and message strings (including special characters, long strings, unicode); render the Alert component and verify both title and message text appear verbatim in the output
    - **Validates: Requirements 11.6**

  - [ ]* 10.6 Write unit tests for error pages and Alert component
    - Test 404 page renders with correct elements and theme-aware images
    - Test 500 page renders with correct elements
    - Test 503 page renders with retry action
    - Test Alert component renders all four variants with correct styling
    - Test Alert component includes `role="alert"` ARIA attribute
    - Test error pages render outside AppLayout (standalone layout)
    - _Requirements: 11.1, 11.2, 11.3, 11.4, 11.5, 11.9, 11.10_

- [x] 11. Configure PWA support
  - [x] 11.1 Configure vite-plugin-pwa in Vite config
    - Update `vite.config.ts` to add the `VitePWA` plugin with manifest configuration: name "GLPI", short_name "GLPI", description "GLPI IT Service Management", start_url "/", display "standalone", background_color "#ffffff", theme_color "#1D4ED8"
    - Configure service worker to cache the application shell (HTML, CSS, JS, static assets)
    - Configure icon entries for 192x192 and 512x512 PNG icons
    - _Requirements: 2.1, 2.2, 2.3, 2.4_

  - [x] 11.2 Create PWA icons and update index.html meta tags
    - Create placeholder PWA icons at `public/icons/icon-192x192.png` and `public/icons/icon-512x512.png`
    - Update `index.html` to include PWA meta tags: viewport, theme-color, apple-touch-icon, description
    - _Requirements: 2.1, 2.5_

- [x] 12. Implement Docker deployment
  - [x] 12.1 Create Nginx configuration
    - Create `frontend/nginx.conf` with:
      - `location /api/` proxying to `http://api-gateway:8080/` for backend API requests
      - `location /` with `try_files $uri $uri/ /index.html` for SPA client-side routing fallback
      - Cache headers: `Cache-Control: public, max-age=31536000, immutable` for hashed assets (`/assets/*`)
      - Cache headers: `Cache-Control: no-cache, no-store, must-revalidate` for `index.html` and `sw.js`
    - _Requirements: 9.3, 9.5, 9.6_

  - [x] 12.2 Create multi-stage Dockerfile
    - Create `frontend/Dockerfile` with:
      - Stage 1 (build): Node.js image, copy source, accept `API_GATEWAY_URL`, `APP_VERSION`, `PUBLIC_URL` as build args, set them as `VITE_API_GATEWAY_URL` and `VITE_APP_VERSION` env vars, run `npm ci && npm run build`
      - Stage 2 (serve): Nginx image, copy built static files from stage 1, copy `nginx.conf`, expose port 80
    - _Requirements: 9.1, 9.2_

  - [x] 12.3 Verify docker-compose frontend service configuration
    - Verify the existing `frontend` service in `docker-compose.yml` matches the design: build context `./frontend`, Dockerfile `Dockerfile`, build args `API_GATEWAY_URL`, `APP_VERSION`, `PUBLIC_URL`, port mapping `${FRONTEND_PORT}:80`, depends on `api-gateway`
    - Update if any discrepancies exist
    - _Requirements: 9.4_

- [x] 13. Finalize build configuration and environment
  - [x] 13.1 Update Vite configuration
    - Ensure `vite.config.ts` includes React plugin (`@vitejs/plugin-react`), SVGR plugin (`vite-plugin-svgr`), and PWA plugin
    - Configure dev server proxy: `/api` → `http://localhost:8080` for local development
    - Ensure production build produces hashed filenames for cache-busting
    - _Requirements: 10.1, 10.2, 10.3, 10.5_

  - [x] 13.2 Configure Vitest for testing
    - Create or update `vitest.config.ts` with jsdom environment, globals enabled, and setup file at `src/test/setup.ts`
    - Create `src/test/setup.ts` with `@testing-library/jest-dom` imports and any MSW server setup
    - _Requirements: 10.1_

- [x] 14. Final checkpoint — Ensure all tests pass
  - Run `npm run build` to verify production build succeeds
  - Run `npm run test` to verify all tests pass
  - Ensure all tests pass, ask the user if questions arise

- [-] 15. Version control and release
  - [ ] Ensure all previous tasks are complete and tests pass
  - [ ] Remove SNAPSHOT suffix from all version references in the codebase
  - [ ] Commit the version bump: "release: 1.2.0 - glpi-pwa-frontend"
  - [ ] Merge branch into main/master
  - [ ] Apply Git tag: 1.2.0 (without SNAPSHOT)
  - [ ] Push branch, merge, and tag to remote

## Notes

- Tasks marked with `*` are optional and can be skipped for faster MVP
- Each task references specific requirements for traceability
- Checkpoints ensure incremental validation
- Property tests validate universal correctness properties from the design document
- Unit tests validate specific examples and edge cases
- The `.frontend-template/` directory is read-only — copy and adapt, never modify
- All code uses TypeScript + React 19 + Tailwind CSS v4 + Vite
