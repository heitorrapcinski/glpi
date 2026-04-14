/**
 * Status display configuration and timeline/SLA color utilities.
 *
 * Provides lookup maps and helper functions for ITIL object status badges,
 * timeline entry color theming, and SLA deadline indicator colors.
 */

// ---------------------------------------------------------------------------
// Status config
// ---------------------------------------------------------------------------

export interface StatusConfig {
  label: string;
  color: string;
  icon: string;
}

export const STATUS_CONFIG: Record<number, StatusConfig> = {
  1: { label: 'New',      color: '#3b82f6', icon: 'circle' },
  2: { label: 'Assigned', color: '#f97316', icon: 'user-check' },
  3: { label: 'Planned',  color: '#8b5cf6', icon: 'calendar' },
  4: { label: 'Pending',  color: '#eab308', icon: 'clock' },
  5: { label: 'Solved',   color: '#22c55e', icon: 'check-circle' },
  6: { label: 'Closed',   color: '#6b7280', icon: 'lock' },
};

/**
 * Returns the display configuration for a given ITIL status code.
 * Falls back to a neutral "Unknown" entry when the code is not recognised.
 */
export function getStatusConfig(code: number): StatusConfig {
  return (
    STATUS_CONFIG[code] ?? { label: 'Unknown', color: '#9ca3af', icon: 'help-circle' }
  );
}

// ---------------------------------------------------------------------------
// Timeline entry colors
// ---------------------------------------------------------------------------

export interface TimelineEntryColors {
  backgroundColor: string;
  borderColor: string;
  foregroundColor: string;
}

const TIMELINE_COLORS: Record<string, TimelineEntryColors> = {
  itilcontent: {
    backgroundColor: '#e2f2e3',
    borderColor: '#87aa8a',
    foregroundColor: '#155210',
  },
  followup: {
    backgroundColor: '#ececec',
    borderColor: '#b3b3b3',
    foregroundColor: '#535353',
  },
  task: {
    backgroundColor: '#ffe8b9',
    borderColor: '#e5c88c',
    foregroundColor: '#38301f',
  },
  solution: {
    backgroundColor: '#9fd6ed',
    borderColor: '#90c2d8',
    foregroundColor: '#27363b',
  },
  document: {
    backgroundColor: '#80cead',
    borderColor: '#68b997',
    foregroundColor: '#21352c',
  },
  log: {
    backgroundColor: '#cacaca21',
    borderColor: 'transparent',
    foregroundColor: 'inherit',
  },
  validation: {
    backgroundColor: '#e2f2e3',
    borderColor: '#87aa8a',
    foregroundColor: '#155210',
  },
};

/**
 * Returns background, border, and foreground colors for a timeline entry type.
 * Falls back to a neutral gray palette for unrecognised types.
 */
export function getTimelineEntryColors(type: string): TimelineEntryColors {
  return (
    TIMELINE_COLORS[type.toLowerCase()] ?? {
      backgroundColor: '#f3f4f6',
      borderColor: '#d1d5db',
      foregroundColor: '#374151',
    }
  );
}

// ---------------------------------------------------------------------------
// SLA indicator color
// ---------------------------------------------------------------------------

export type SlaIndicatorColor = 'green' | 'orange' | 'red';

/**
 * Determines the SLA indicator color based on how much time remains.
 *
 * @param deadline     - The SLA deadline timestamp (ms since epoch).
 * @param current      - The current timestamp (ms since epoch).
 * @param totalDuration - The total SLA duration in milliseconds.
 * @returns `'green'` when >25 % remaining, `'orange'` when ≤25 % remaining,
 *          or `'red'` when the deadline has already passed.
 */
export function getSlaIndicatorColor(
  deadline: number,
  current: number,
  totalDuration: number,
): SlaIndicatorColor {
  if (current > deadline) {
    return 'red';
  }

  const remaining = deadline - current;
  const percentRemaining = totalDuration > 0 ? remaining / totalDuration : 1;

  return percentRemaining > 0.25 ? 'green' : 'orange';
}
