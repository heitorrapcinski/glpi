import { describe, it, expect } from 'vitest';
import {
  PRIORITY_CONFIG,
  getPriorityConfig,
  computePriority,
} from './priority';

// ---------------------------------------------------------------------------
// getPriorityConfig
// ---------------------------------------------------------------------------

describe('getPriorityConfig', () => {
  it('returns correct config for all 6 priority codes', () => {
    expect(getPriorityConfig(1)).toEqual({ label: 'Very Low', color: '#6b7280' });
    expect(getPriorityConfig(2)).toEqual({ label: 'Low', color: '#3b82f6' });
    expect(getPriorityConfig(3)).toEqual({ label: 'Medium', color: '#f97316' });
    expect(getPriorityConfig(4)).toEqual({ label: 'High', color: '#ef4444' });
    expect(getPriorityConfig(5)).toEqual({ label: 'Very High', color: '#991b1b' });
    expect(getPriorityConfig(6)).toEqual({ label: 'Major', color: '#000000' });
  });

  it('returns fallback for unknown priority code', () => {
    const result = getPriorityConfig(0);
    expect(result.label).toBe('Unknown');
    expect(result.color).toBe('#9ca3af');
  });

  it('PRIORITY_CONFIG has exactly 6 entries', () => {
    expect(Object.keys(PRIORITY_CONFIG)).toHaveLength(6);
  });
});

// ---------------------------------------------------------------------------
// computePriority (ITIL Impact × Urgency matrix)
// ---------------------------------------------------------------------------

describe('computePriority', () => {
  it.each([
    // [impact, urgency, expected priority]
    [1, 1, 1],
    [1, 2, 2],
    [1, 3, 3],
    [2, 1, 2],
    [2, 2, 3],
    [2, 3, 4],
    [3, 1, 3],
    [3, 2, 4],
    [3, 3, 5],
  ])('impact=%i urgency=%i → priority %i', (impact, urgency, expected) => {
    expect(computePriority(impact, urgency)).toBe(expected);
  });

  it('returns Medium (3) for out-of-range impact', () => {
    expect(computePriority(0, 2)).toBe(3);
    expect(computePriority(4, 2)).toBe(3);
  });

  it('returns Medium (3) for out-of-range urgency', () => {
    expect(computePriority(2, 0)).toBe(3);
    expect(computePriority(2, 4)).toBe(3);
  });
});
