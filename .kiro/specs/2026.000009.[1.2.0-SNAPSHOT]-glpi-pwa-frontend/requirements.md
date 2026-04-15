# Requirements Document

## Introduction

This document defines the requirements for the GLPI PWA Frontend MVP — a Progressive Web App that provides a login page and dashboard page for the GLPI microservices backend. The frontend is built with React 19, TypeScript, and Tailwind CSS v4, adapted from the TailAdmin React v2.1.0 template. It runs in the browser (desktop and mobile) and can be installed as a standalone app via PWA capabilities. The scope is strictly limited to authentication (login, token management, logout) and a dashboard landing page.

## Glossary

- **Frontend**: The GLPI PWA React application served from the `frontend/` directory
- **API_Gateway**: The backend edge service running on port 8080 that handles JWT validation, rate limiting, and request routing
- **Auth_Service**: The identity microservice behind the API_Gateway that processes authentication requests at `/auth/*` endpoints
- **Login_Page**: The authentication page where users enter credentials to obtain a JWT access token
- **Dashboard_Page**: The main landing page displayed after successful authentication, showing summary widgets
- **JWT**: JSON Web Token (RS256) used for stateless authentication between the Frontend and the API_Gateway
- **Access_Token**: A short-lived JWT (default 3600 seconds) used in the `Authorization: Bearer` header for authenticated API requests
- **Refresh_Token**: A longer-lived token (default 604800 seconds) used to obtain a new Access_Token without re-authenticating
- **TOTP_Code**: A Time-based One-Time Password used as an optional second factor during login
- **PWA**: Progressive Web App — a web application that uses service workers, a manifest, and HTTPS to provide installable, offline-capable experiences
- **Service_Worker**: A background script registered by the Frontend that enables PWA features such as caching and install prompts
- **Web_App_Manifest**: A JSON file (`manifest.json`) that describes the PWA metadata (name, icons, theme color, display mode) for install prompts
- **Auth_Context**: A React context provider that manages authentication state (tokens, user session) across the Frontend
- **Protected_Route**: A route wrapper that redirects unauthenticated users to the Login_Page
- **Token_Storage**: The browser storage mechanism (memory or secure storage) used by the Frontend to persist JWT tokens across page reloads
- **Nginx**: The web server used in the Docker container to serve the Frontend static build files on port 80
- **Docker_Container**: The containerized deployment unit for the Frontend, exposed on port 3000 via docker-compose

## Requirements

### Requirement 1: Project Scaffolding from Template

**User Story:** As a developer, I want the frontend project scaffolded from the TailAdmin React template into the `frontend/` directory, so that I have a working React 19 + TypeScript + Tailwind CSS v4 + Vite foundation without modifying the read-only template.

#### Acceptance Criteria

1. THE Frontend SHALL be located in the `frontend/` directory at the workspace root
2. THE Frontend SHALL use React 19, TypeScript, Tailwind CSS v4, and Vite as its technology stack, matching the TailAdmin React v2.1.0 template
3. THE Frontend SHALL include only the pages, components, and assets required for the Login_Page and Dashboard_Page (MVP scope)
4. THE Frontend SHALL remove all template pages and components not related to authentication or the dashboard (e.g., calendar, forms, tables, charts, ecommerce, user profile pages)
5. THE Frontend SHALL preserve the ThemeContext (dark mode support) and SidebarContext from the template

### Requirement 2: PWA Configuration

**User Story:** As a user, I want the application to be installable as a PWA on desktop and mobile devices, so that I can access it like a native app.

#### Acceptance Criteria

1. THE Frontend SHALL include a valid Web_App_Manifest file with the application name "GLPI", theme color, background color, display mode set to "standalone", start URL, and at least two icon sizes (192x192 and 512x512)
2. THE Frontend SHALL register a Service_Worker that caches the application shell (HTML, CSS, JS, and static assets) for offline loading of the app shell
3. WHEN the Frontend is loaded in a compatible browser, THE Frontend SHALL be eligible for the browser's PWA install prompt
4. WHEN the Frontend is installed as a PWA, THE Frontend SHALL launch in standalone display mode without the browser address bar
5. THE Frontend SHALL include appropriate `<meta>` tags in `index.html` for PWA support (viewport, theme-color, apple-touch-icon, description)

### Requirement 3: Authentication — Login

**User Story:** As a user, I want to log in with my username and password (and optional TOTP code), so that I can access the GLPI dashboard.

#### Acceptance Criteria

1. THE Login_Page SHALL display input fields for username and password, and a submit button
2. THE Login_Page SHALL display an optional TOTP_Code input field for two-factor authentication
3. WHEN the user submits valid credentials, THE Frontend SHALL send a POST request to `/auth/login` on the API_Gateway with the payload `{ username, password, totpCode? }`
4. WHEN the API_Gateway returns a successful response with `{ accessToken, refreshToken, expiresIn }`, THE Frontend SHALL store the Access_Token and Refresh_Token in Token_Storage and navigate the user to the Dashboard_Page
5. WHEN the API_Gateway returns an HTTP 401 response, THE Login_Page SHALL display an inline error message indicating invalid credentials
6. WHEN the API_Gateway returns an HTTP 429 response, THE Login_Page SHALL display an inline error message indicating rate limiting and advise the user to wait
7. IF the API_Gateway is unreachable or returns an HTTP 5xx response, THEN THE Login_Page SHALL display an inline error message indicating a server connectivity problem
8. WHILE the login request is in progress, THE Login_Page SHALL disable the submit button and display a loading indicator
9. THE Login_Page SHALL validate that the username and password fields are non-empty before sending the request to the API_Gateway

### Requirement 4: Authentication — Token Management

**User Story:** As a user, I want my session to remain active without re-entering credentials, so that I have a seamless experience while using the application.

#### Acceptance Criteria

1. THE Auth_Context SHALL store the Access_Token, Refresh_Token, and token expiration timestamp in memory and persist them in Token_Storage across page reloads
2. WHEN the Access_Token is within 60 seconds of expiration, THE Auth_Context SHALL automatically send a POST request to `/auth/refresh` on the API_Gateway with the current Refresh_Token to obtain a new token pair
3. WHEN the token refresh succeeds, THE Auth_Context SHALL replace the stored Access_Token and Refresh_Token with the new values
4. IF the token refresh fails with an HTTP 401 response, THEN THE Auth_Context SHALL clear all stored tokens and redirect the user to the Login_Page
5. THE Frontend SHALL attach the Access_Token as an `Authorization: Bearer {accessToken}` header to every authenticated API request sent to the API_Gateway
6. WHEN the user opens the Frontend and a valid (non-expired) Access_Token exists in Token_Storage, THE Frontend SHALL restore the session and navigate to the Dashboard_Page without requiring re-authentication

### Requirement 5: Authentication — Logout

**User Story:** As a user, I want to log out of the application, so that my session is terminated and my tokens are invalidated.

#### Acceptance Criteria

1. THE Dashboard_Page layout SHALL display a logout control accessible from the application header
2. WHEN the user activates the logout control, THE Frontend SHALL send a POST request to `/auth/logout` on the API_Gateway with the `Authorization: Bearer {accessToken}` header
3. WHEN the logout request completes (regardless of success or failure), THE Auth_Context SHALL clear all stored tokens from Token_Storage and redirect the user to the Login_Page
4. AFTER logout, WHEN the user navigates to any Protected_Route, THE Frontend SHALL redirect the user to the Login_Page

### Requirement 6: Route Protection

**User Story:** As a developer, I want unauthenticated users to be redirected to the login page, so that protected content is not accessible without a valid session.

#### Acceptance Criteria

1. THE Frontend SHALL wrap the Dashboard_Page and all future authenticated pages inside a Protected_Route component
2. WHEN an unauthenticated user navigates to a Protected_Route, THE Frontend SHALL redirect the user to the Login_Page
3. WHEN an authenticated user navigates to the Login_Page, THE Frontend SHALL redirect the user to the Dashboard_Page
4. THE Frontend SHALL define only two routes for the MVP: the Login_Page route (`/signin`) and the Dashboard_Page route (`/`)
5. WHEN the user navigates to an undefined route, THE Frontend SHALL display the 404 error page as defined in Requirement 11

### Requirement 7: Dashboard Page

**User Story:** As an authenticated user, I want to see a dashboard after logging in, so that I have a landing page confirming successful access.

#### Acceptance Criteria

1. THE Dashboard_Page SHALL display a welcome message that includes the authenticated user context
2. THE Dashboard_Page SHALL use the AppLayout structure from the template (sidebar, header, content area)
3. THE Dashboard_Page SHALL display placeholder widget cards in a responsive grid layout as a foundation for future ITSM dashboard content
4. THE Dashboard_Page SHALL support both light and dark themes via the ThemeContext toggle in the header
5. THE Dashboard_Page layout SHALL be responsive, adapting from a sidebar layout on desktop to a collapsible menu on mobile viewports

### Requirement 8: API Communication Layer

**User Story:** As a developer, I want a centralized HTTP client configured for the API Gateway, so that all API calls use consistent base URL, headers, and error handling.

#### Acceptance Criteria

1. THE Frontend SHALL use a centralized HTTP client configured with the API_Gateway base URL provided via the `VITE_API_GATEWAY_URL` environment variable
2. THE Frontend SHALL automatically attach the `Authorization: Bearer {accessToken}` header to all requests made through the centralized HTTP client when an Access_Token is available
3. WHEN the centralized HTTP client receives an HTTP 401 response on a non-authentication endpoint, THE Frontend SHALL attempt a token refresh and retry the original request once
4. IF the retry after token refresh also fails with HTTP 401, THEN THE Frontend SHALL clear the session and redirect the user to the Login_Page
5. THE Frontend SHALL read the API_Gateway base URL from the `VITE_API_GATEWAY_URL` environment variable at build time, defaulting to `/api` when the variable is not set

### Requirement 9: Docker Deployment

**User Story:** As a DevOps engineer, I want the frontend packaged as a Docker container served by Nginx, so that it integrates with the existing docker-compose infrastructure.

#### Acceptance Criteria

1. THE Docker_Container SHALL use a multi-stage Dockerfile: a Node.js build stage that compiles the Frontend, and an Nginx stage that serves the static output on port 80
2. THE Docker_Container SHALL accept `API_GATEWAY_URL`, `APP_VERSION`, and `PUBLIC_URL` as build arguments
3. THE Nginx configuration SHALL proxy requests matching `/api/*` to the API_Gateway service URL, enabling the Frontend to call backend endpoints without CORS issues in production
4. THE Docker_Container SHALL be defined in `docker-compose.yml` as the `frontend` service, exposed on port 3000, depending on the `api-gateway` service
5. THE Nginx configuration SHALL serve `index.html` for all non-file routes to support client-side routing by the Frontend
6. THE Nginx configuration SHALL set appropriate cache headers: long-lived caching for hashed static assets and no-cache for `index.html` and the Service_Worker script

### Requirement 10: Build Configuration and Environment

**User Story:** As a developer, I want the Vite build properly configured with environment variables and PWA support, so that the application builds correctly for both development and production.

#### Acceptance Criteria

1. THE Frontend SHALL use Vite as the build tool with the React plugin and SVGR plugin matching the template configuration
2. THE Frontend SHALL support the following environment variables at build time: `VITE_API_GATEWAY_URL` (API Gateway base URL) and `VITE_APP_VERSION` (application version string)
3. THE Frontend SHALL produce a production build with hashed filenames for cache-busting of JavaScript and CSS assets
4. THE Frontend SHALL include a `.env.example` file in the `frontend/` directory documenting all required environment variables
5. WHEN running in development mode, THE Frontend SHALL proxy API requests to `http://localhost:8080` to enable local development against the backend

### Requirement 11: Error Pages and Alert Components

**User Story:** As a user, I want to see clear, themed error pages when something goes wrong and inline alert messages for contextual feedback, so that I understand what happened and what action to take.

#### Acceptance Criteria

1. WHEN the user navigates to an undefined route, THE Frontend SHALL display a 404 Not Found error page with an illustrative SVG image, an error heading, a descriptive message, and a link to navigate back to the Dashboard_Page
2. THE 404 error page SHALL use the template error images (`404.svg` for light mode and `404-dark.svg` for dark mode) and support theme switching via the ThemeContext
3. WHEN the API_Gateway returns an HTTP 500 response on a non-authentication endpoint, THE Frontend SHALL display a 500 Internal Server Error page with an illustrative SVG image (`500.svg` / `500-dark.svg`), an error heading, a descriptive message, and a link to navigate back to the Dashboard_Page
4. WHEN the API_Gateway returns an HTTP 503 response or is unreachable on a non-authentication endpoint, THE Frontend SHALL display a 503 Service Unavailable page with an illustrative SVG image (`503.svg` / `503-dark.svg`), an error heading, a descriptive message, and a retry action
5. THE Frontend SHALL include a reusable Alert component that supports four variants: success, error, warning, and info, each with a distinct icon, border color, and background color
6. THE Alert component SHALL accept a title, a message, and an optional action link, and SHALL render them consistently across all variants
7. THE Alert component SHALL support both light and dark themes via the ThemeContext
8. THE Frontend SHALL use the Alert component for all inline contextual feedback messages, including login errors (Requirement 3 AC5, AC6, AC7), token refresh failures (Requirement 4 AC4), and API error responses (Requirement 8 AC3, AC4)
9. THE error pages (404, 500, 503) SHALL be rendered as full-page layouts outside the AppLayout, consistent with the Login_Page layout pattern
10. THE error pages and Alert component SHALL be accessible, providing appropriate ARIA roles (`role="alert"` for alerts, descriptive alt text for error images) and sufficient color contrast in both light and dark themes

