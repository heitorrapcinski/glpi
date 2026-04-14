import { useQuery, keepPreviousData } from '@tanstack/react-query';
import api from '../api/client';
import { DASHBOARD } from '../api/endpoints';

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

export interface DashboardCounters {
  openTickets: number;
  myAssignedTickets: number;
  overdueTickets: number;
  pendingTickets: number;
}

export interface StatusChartEntry {
  status: number;
  label: string;
  count: number;
}

export interface PriorityChartEntry {
  priority: number;
  label: string;
  count: number;
}

export interface ActivityFeedEntry {
  id: string;
  type: string;
  objectType: string;
  objectId: string;
  objectTitle: string;
  authorName: string;
  createdAt: string;
}

export interface DashboardStats {
  counters: DashboardCounters;
  ticketsByStatus: StatusChartEntry[];
  ticketsByPriority: PriorityChartEntry[];
  recentActivity: ActivityFeedEntry[];
}

// ---------------------------------------------------------------------------
// Query key factory
// ---------------------------------------------------------------------------
export const dashboardKeys = {
  all: ['dashboard'] as const,
  stats: () => [...dashboardKeys.all, 'stats'] as const,
};

// ---------------------------------------------------------------------------
// Hooks
// ---------------------------------------------------------------------------

/** Dashboard widget data: counters, charts, activity feed */
export function useDashboardStats() {
  return useQuery({
    queryKey: dashboardKeys.stats(),
    queryFn: () => api.get<DashboardStats>(DASHBOARD.STATS),
    placeholderData: keepPreviousData,
    staleTime: 30_000, // 30s — dashboard data doesn't need instant freshness
  });
}
