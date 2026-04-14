import React, { Suspense } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import LoadingSkeleton from './components/common/LoadingSkeleton';
import LoginPage from './pages/LoginPage';
import AuthLayout from './layouts/AuthLayout';
import CentralLayout from './layouts/CentralLayout';
import HelpdeskLayout from './layouts/HelpdeskLayout';

// ---------------------------------------------------------------------------
// Lazy-loaded page components
// ---------------------------------------------------------------------------

const DashboardPage = React.lazy(() => import('./pages/DashboardPage'));
const HelpdeskHomePage = React.lazy(() => import('./pages/HelpdeskHomePage'));

const TicketListPage = React.lazy(() => import('./pages/tickets/TicketListPage'));
const TicketCreatePage = React.lazy(() => import('./pages/tickets/TicketCreatePage'));
const TicketDetailPage = React.lazy(() => import('./pages/tickets/TicketDetailPage'));

const ProblemListPage = React.lazy(() => import('./pages/problems/ProblemListPage'));
const ProblemCreatePage = React.lazy(() => import('./pages/problems/ProblemCreatePage'));
const ProblemDetailPage = React.lazy(() => import('./pages/problems/ProblemDetailPage'));

const ChangeListPage = React.lazy(() => import('./pages/changes/ChangeListPage'));
const ChangeCreatePage = React.lazy(() => import('./pages/changes/ChangeCreatePage'));
const ChangeDetailPage = React.lazy(() => import('./pages/changes/ChangeDetailPage'));

const AssetListPage = React.lazy(() => import('./pages/assets/AssetListPage'));
const AssetDetailPage = React.lazy(() => import('./pages/assets/AssetDetailPage'));
const SoftwareListPage = React.lazy(() => import('./pages/assets/SoftwareListPage'));
const LicenseListPage = React.lazy(() => import('./pages/assets/LicenseListPage'));

const KnowledgeListPage = React.lazy(() => import('./pages/knowledge/KnowledgeListPage'));
const KnowledgeDetailPage = React.lazy(() => import('./pages/knowledge/KnowledgeDetailPage'));

const PreferencesPage = React.lazy(() => import('./pages/PreferencesPage'));

// ---------------------------------------------------------------------------
// Helpdesk-allowed routes — paths accessible when profile is "helpdesk"
// ---------------------------------------------------------------------------

const HELPDESK_ALLOWED_PATHS = [
  '/helpdesk',
  '/tickets',
  '/tickets/new',
  '/knowledge',
  '/preferences',
];

/**
 * Returns true when the given path is allowed for a helpdesk profile.
 * Parameterised routes like `/tickets/:id` and `/knowledge/:id` are matched
 * by checking that the path starts with the base segment.
 */
export function isHelpdeskRouteAllowed(path: string): boolean {
  // Exact matches
  if (HELPDESK_ALLOWED_PATHS.includes(path)) return true;

  // Parameterised matches: /tickets/:id, /knowledge/:id
  if (/^\/tickets\/[^/]+$/.test(path)) return true;
  if (/^\/knowledge\/[^/]+$/.test(path)) return true;

  return false;
}

// ---------------------------------------------------------------------------
// RequireAuth — redirects unauthenticated users to /login
// ---------------------------------------------------------------------------

interface RequireAuthProps {
  children: React.ReactNode;
}

function RequireAuth({ children }: RequireAuthProps) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const location = useLocation();

  if (!isAuthenticated) {
    const returnTo = location.pathname + location.search;
    return <Navigate to={`/login?returnTo=${encodeURIComponent(returnTo)}`} replace />;
  }

  return <>{children}</>;
}

// ---------------------------------------------------------------------------
// HelpdeskGuard — restricts helpdesk profiles to allowed routes
// ---------------------------------------------------------------------------

function HelpdeskGuard({ children }: { children: React.ReactNode }) {
  const profileInterface = useAuthStore((s) => s.user?.profileInterface);
  const location = useLocation();

  if (profileInterface === 'helpdesk' && !isHelpdeskRouteAllowed(location.pathname)) {
    return <Navigate to="/helpdesk" replace />;
  }

  return <>{children}</>;
}

// ---------------------------------------------------------------------------
// ProfileRedirect — redirects "/" to the correct home based on profile
// ---------------------------------------------------------------------------

function ProfileRedirect() {
  const profileInterface = useAuthStore((s) => s.user?.profileInterface);

  if (profileInterface === 'helpdesk') {
    return <Navigate to="/helpdesk" replace />;
  }

  // Default to dashboard for central (or when profile is not yet loaded)
  return <Navigate to="/dashboard" replace />;
}

// ---------------------------------------------------------------------------
// Lazy — wraps children in Suspense with loading skeleton
// ---------------------------------------------------------------------------

function Lazy({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<LoadingSkeleton />}>{children}</Suspense>;
}

// ---------------------------------------------------------------------------
// LayoutSwitch — renders CentralLayout or HelpdeskLayout based on profile
// ---------------------------------------------------------------------------

function LayoutSwitch() {
  const profileInterface = useAuthStore((s) => s.user?.profileInterface);

  if (profileInterface === 'helpdesk') {
    return <HelpdeskLayout />;
  }

  return <CentralLayout />;
}

// ---------------------------------------------------------------------------
// AppRoutes — all application routes
// ---------------------------------------------------------------------------

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public — Auth layout */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
      </Route>

      {/* Protected routes — profile-based layout */}
      <Route
        element={
          <RequireAuth>
            <HelpdeskGuard>
              <LayoutSwitch />
            </HelpdeskGuard>
          </RequireAuth>
        }
      >
        {/* Profile-based root redirect */}
        <Route path="/" element={<ProfileRedirect />} />

        {/* Dashboard */}
        <Route path="/dashboard" element={<Lazy><DashboardPage /></Lazy>} />

        {/* Helpdesk home */}
        <Route path="/helpdesk" element={<Lazy><HelpdeskHomePage /></Lazy>} />

        {/* Tickets */}
        <Route path="/tickets" element={<Lazy><TicketListPage /></Lazy>} />
        <Route path="/tickets/new" element={<Lazy><TicketCreatePage /></Lazy>} />
        <Route path="/tickets/:id" element={<Lazy><TicketDetailPage /></Lazy>} />

        {/* Problems */}
        <Route path="/problems" element={<Lazy><ProblemListPage /></Lazy>} />
        <Route path="/problems/new" element={<Lazy><ProblemCreatePage /></Lazy>} />
        <Route path="/problems/:id" element={<Lazy><ProblemDetailPage /></Lazy>} />

        {/* Changes */}
        <Route path="/changes" element={<Lazy><ChangeListPage /></Lazy>} />
        <Route path="/changes/new" element={<Lazy><ChangeCreatePage /></Lazy>} />
        <Route path="/changes/:id" element={<Lazy><ChangeDetailPage /></Lazy>} />

        {/* Assets */}
        <Route path="/assets" element={<Lazy><AssetListPage /></Lazy>} />
        <Route path="/assets/software" element={<Lazy><SoftwareListPage /></Lazy>} />
        <Route path="/assets/licenses" element={<Lazy><LicenseListPage /></Lazy>} />
        <Route path="/assets/:type/:id" element={<Lazy><AssetDetailPage /></Lazy>} />

        {/* Knowledge */}
        <Route path="/knowledge" element={<Lazy><KnowledgeListPage /></Lazy>} />
        <Route path="/knowledge/:id" element={<Lazy><KnowledgeDetailPage /></Lazy>} />

        {/* Preferences */}
        <Route path="/preferences" element={<Lazy><PreferencesPage /></Lazy>} />
      </Route>

      {/* Catch-all — redirect to root for profile-based routing */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
