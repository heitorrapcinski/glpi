import type { CSSProperties } from 'react';
import { useDashboardStats } from '@/hooks/useDashboard';
import CounterWidget from '@/components/dashboard/CounterWidget';
import BarChartWidget from '@/components/dashboard/BarChartWidget';
import PieChartWidget from '@/components/dashboard/PieChartWidget';
import LoadingSkeleton from '@/components/common/LoadingSkeleton';

// ---------------------------------------------------------------------------
// DashboardPage — Default landing page for Central_Interface
// ---------------------------------------------------------------------------

const page: CSSProperties = {
  padding: '1.5rem',
  maxWidth: '1400px',
  margin: '0 auto',
};

const title: CSSProperties = {
  fontSize: '1.25rem',
  fontWeight: 700,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: '0 0 1.5rem 0',
};

const countersGrid: CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
  gap: '1rem',
  marginBottom: '1.5rem',
};

const chartsGrid: CSSProperties = {
  display: 'grid',
  gridTemplateColumns: 'repeat(auto-fit, minmax(340px, 1fr))',
  gap: '1.5rem',
  marginBottom: '1.5rem',
};

const activityCard: CSSProperties = {
  backgroundColor: 'var(--tblr-bg-surface, #fff)',
  borderRadius: '8px',
  boxShadow: '0 1px 3px rgba(0,0,0,0.08)',
  padding: '1.25rem 1.5rem',
};

const activityHeading: CSSProperties = {
  fontSize: '0.9375rem',
  fontWeight: 600,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: '0 0 1rem 0',
};

const activityItem: CSSProperties = {
  display: 'flex',
  justifyContent: 'space-between',
  alignItems: 'flex-start',
  padding: '0.625rem 0',
  borderBottom: '1px solid #e9ecef',
  gap: '0.75rem',
};

const activityTitle: CSSProperties = {
  fontSize: '0.8125rem',
  fontWeight: 500,
  color: 'var(--tblr-body-color, #1e293b)',
  margin: 0,
};

const activityMeta: CSSProperties = {
  fontSize: '0.75rem',
  color: 'var(--tblr-secondary, #606f91)',
  margin: 0,
  whiteSpace: 'nowrap',
};

const errorBox: CSSProperties = {
  padding: '2rem',
  textAlign: 'center',
  color: '#ef4444',
};

export default function DashboardPage() {
  const { data, isLoading, isError } = useDashboardStats();

  if (isLoading) return <LoadingSkeleton />;

  if (isError || !data?.data) {
    return (
      <div style={errorBox} role="alert">
        <p>Failed to load dashboard data. Please try again later.</p>
      </div>
    );
  }

  const stats = data.data;

  return (
    <div style={page}>
      <h1 style={title}>Dashboard</h1>

      {/* Counter widgets */}
      <div style={countersGrid}>
        <CounterWidget
          label="Open Tickets"
          value={stats.counters.openTickets}
          color="#3b82f6"
          href="/tickets?status=open"
          icon="📋"
        />
        <CounterWidget
          label="My Assigned"
          value={stats.counters.myAssignedTickets}
          color="#f97316"
          href="/tickets?assigned=me"
          icon="👤"
        />
        <CounterWidget
          label="Overdue"
          value={stats.counters.overdueTickets}
          color="#ef4444"
          href="/tickets?status=overdue"
          icon="⏰"
        />
        <CounterWidget
          label="Pending"
          value={stats.counters.pendingTickets}
          color="#eab308"
          href="/tickets?status=pending"
          icon="⏳"
        />
      </div>

      {/* Charts */}
      <div style={chartsGrid}>
        <BarChartWidget data={stats.ticketsByStatus} />
        <PieChartWidget data={stats.ticketsByPriority} />
      </div>

      {/* Recent activity feed */}
      {stats.recentActivity.length > 0 && (
        <div style={activityCard}>
          <h3 style={activityHeading}>Recent Activity</h3>
          {stats.recentActivity.map((entry) => (
            <div key={entry.id} style={activityItem}>
              <div style={{ minWidth: 0 }}>
                <p style={activityTitle}>
                  {entry.objectType} #{entry.objectId} — {entry.objectTitle}
                </p>
                <p style={activityMeta}>
                  {entry.type} by {entry.authorName}
                </p>
              </div>
              <time style={activityMeta} dateTime={entry.createdAt}>
                {new Date(entry.createdAt).toLocaleDateString('en-US', {
                  month: 'short',
                  day: 'numeric',
                  hour: '2-digit',
                  minute: '2-digit',
                })}
              </time>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
