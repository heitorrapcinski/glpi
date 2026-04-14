/**
 * Priority display configuration and ITIL priority matrix computation.
 *
 * Provides a lookup map for priority badge rendering and a function that
 * derives priority from the standard ITIL Impact × Urgency matrix.
 */

// ---------------------------------------------------------------------------
// Priority config
// ---------------------------------------------------------------------------

export interface PriorityConfig {
  label: string;
  color: string;
}

export const PRIORITY_CONFIG: Record<number, PriorityConfig> = {
  1: { label: 'Very Low',  color: '#6b7280' },
  2: { label: 'Low',       color: '#3b82f6' },
  3: { label: 'Medium',    color: '#f97316' },
  4: { label: 'High',      color: '#ef4444' },
  5: { label: 'Very High', color: '#991b1b' },
  6: { label: 'Major',     color: '#000000' },
};

/**
 * Returns the display configuration for a given priority code.
 * Falls back to a neutral "Unknown" entry when the code is not recognised.
 */
export function getPriorityConfig(code: number): PriorityConfig {
  return (
    PRIORITY_CONFIG[code] ?? { label: 'Unknown', color: '#9ca3af' }
  );
}

// ---------------------------------------------------------------------------
// Priority matrix (Impact × Urgency)
// ---------------------------------------------------------------------------

/**
 * ITIL priority matrix — maps (impact, urgency) pairs to a priority code.
 *
 * | Impact \ Urgency | Low (1) | Medium (2) | High (3) |
 * |------------------|---------|------------|----------|
 * | Low (1)          | 1       | 2          | 3        |
 * | Medium (2)       | 2       | 3          | 4        |
 * | High (3)         | 3       | 4          | 5        |
 */
const PRIORITY_MATRIX: Record<number, Record<number, number>> = {
  1: { 1: 1, 2: 2, 3: 3 },
  2: { 1: 2, 2: 3, 3: 4 },
  3: { 1: 3, 2: 4, 3: 5 },
};

/**
 * Computes the ITIL priority from impact and urgency values.
 *
 * @param impact  - Impact level (1 = Low, 2 = Medium, 3 = High).
 * @param urgency - Urgency level (1 = Low, 2 = Medium, 3 = High).
 * @returns The computed priority code (1–5), or `3` (Medium) when the
 *          combination is outside the defined matrix.
 */
export function computePriority(impact: number, urgency: number): number {
  return PRIORITY_MATRIX[impact]?.[urgency] ?? 3;
}
