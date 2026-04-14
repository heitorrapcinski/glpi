import React, { Suspense } from 'react';
import { Navigate, Route, Routes, useLocation } from 'react-router-dom';
import { useAuthStore } from './stores/authStore';
import LoadingSkeleton from './components/common/LoadingSkeleton';
import LoginPage from './pages/LoginPage';

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
// AuthRoute — wraps a lazy page with RequireAuth + HelpdeskGuard + Suspense
// ---------------------------------------------------------------------------

function AuthRoute({ children }: { children: React.ReactNode }) {
  return (
    <RequireAuth>
      <HelpdeskGuard>
        <Suspense fallback={<LoadingSkeleton />}>
          {children}
        </Suspense>
      </HelpdeskGuard>
    </RequireAuth>
  );
}

// ---------------------------------------------------------------------------
// AppRoutes — all application routes
// ---------------------------------------------------------------------------

export default function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/login" element={<LoginPage />} />

      {/* Profile-based root redirect */}
      <Route path="/" element={<RequireAuth><ProfileRedirect /></RequireAuth>} />

      {/* Dashboard */}
      <Route path="/dashboard" element={<AuthRoute><DashboardPage /></AuthRoute>} />

      {/* Helpdesk home */}
      <Route path="/helpdesk" element={<AuthRoute><HelpdeskHomePage /></AuthRoute>} />

      {/* Tickets */}
      <Route path="/tickets" element={<AuthRoute><TicketListPage /></AuthRoute>} />
      <Route path="/tickets/new" element={<AuthRoute><TicketCreatePage /></AuthRoute>} />
      <Route path="/tickets/:id" element={<AuthRoute><TicketDetailPage /></AuthRoute>} />

      {/* Problems */}
      <Route path="/problems" element={<AuthRoute><ProblemListPage /></AuthRoute>} />
      <Route path="/problems/new" element={<AuthRoute><ProblemCreatePage /></AuthRoute>} />
      <Route path="/problems/:id" element={<AuthRoute><ProblemDetailPage /></AuthRoute>} />

      {/* Changes */}
      <Route path="/changes" element={<AuthRoute><ChangeListPage /></AuthRoute>} />
      <Route path="/changes/new" element={<AuthRoute><ChangeCreatePage /></AuthRoute>} />
      <Route path="/changes/:id" element={<AuthRoute><ChangeDetailPage /></AuthRoute>} />

      {/* Assets */}
      <Route path="/assets" element={<AuthRoute><AssetListPage /></AuthRoute>} />
      <Route path="/assets/software" element={<AuthRoute><SoftwareListPage /></AuthRoute>} />
      <Route path="/assets/licenses" element={<AuthRoute><LicenseListPage /></AuthRoute>} />
      <Route path="/assets/:type/:id" element={<AuthRoute><AssetDetailPage /></AuthRoute>} />

      {/* Knowledge */}
      <Route path="/knowledge" element={<AuthRoute><KnowledgeListPage /></AuthRoute>} />
      <Route path="/knowledge/:id" element={<AuthRoute><KnowledgeDetailPage /></AuthRoute>} />

      {/* Preferences */}
      <Route path="/preferences" element={<AuthRoute><PreferencesPage /></AuthRoute>} />

      {/* Catch-all — redirect to root for profile-based routing */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
