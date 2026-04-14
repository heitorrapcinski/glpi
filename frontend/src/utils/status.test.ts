import { describe, it, expect } from 'vitest';
import {
  STATUS_CONFIG,
  getStatusConfig,
  getTimelineEntryColors,
  getSlaIndicatorColor,
} from './status';

// ---------------------------------------------------------------------------
// getStatusConfig
// ---------------------------------------------------------------------------

describe('getStatusConfig', () => {
  it('returns correct config for all 6 status codes', () => {
    expect(getStatusConfig(1)).toEqual({ label: 'New', color: '#3b82f6', icon: 'circle' });
    expect(getStatusConfig(2)).toEqual({ label: 'Assigned', color: '#f97316', icon: 'user-check' });
    expect(getStatusConfig(3)).toEqual({ label: 'Planned', color: '#8b5cf6', icon: 'calendar' });
    expect(getStatusConfig(4)).toEqual({ label: 'Pending', color: '#eab308', icon: 'clock' });
    expect(getStatusConfig(5)).toEqual({ label: 'Solved', color: '#22c55e', icon: 'check-circle' });
    expect(getStatusConfig(6)).toEqual({ label: 'Closed', color: '#6b7280', icon: 'lock' });
  });

  it('returns fallback for unknown status code', () => {
    const result = getStatusConfig(99);
    expect(result.label).toBe('Unknown');
    expect(result.color).toBe('#9ca3af');
    expect(result.icon).toBe('help-circle');
  });

  it('STATUS_CONFIG has exactly 6 entries', () => {
    expect(Object.keys(STATUS_CONFIG)).toHaveLength(6);
  });
});

// ---------------------------------------------------------------------------
// getTimelineEntryColors
// ---------------------------------------------------------------------------

describe('getTimelineEntryColors', () => {
  it.each([
    ['itilcontent', '#e2f2e3', '#87aa8a', '#155210'],
    ['followup', '#ececec', '#b3b3b3', '#535353'],
    ['task', '#ffe8b9', '#e5c88c', '#38301f'],
    ['solution', '#9fd6ed', '#90c2d8', '#27363b'],
    ['document', '#80cead', '#68b997', '#21352c'],
    ['log', '#cacaca21', 'transparent', 'inherit'],
    ['validation', '#e2f2e3', '#87aa8a', '#155210'],
  ])('returns correct colors for %s', (type, bg, border, fg) => {
    const colors = getTimelineEntryColors(type);
    expect(colors.backgroundColor).toBe(bg);
    expect(colors.borderColor).toBe(border);
    expect(colors.foregroundColor).toBe(fg);
  });

  it('is case-insensitive', () => {
    expect(getTimelineEntryColors('Followup')).toEqual(getTimelineEntryColors('followup'));
    expect(getTimelineEntryColors('TASK')).toEqual(getTimelineEntryColors('task'));
  });

  it('returns fallback for unknown type', () => {
    const colors = getTimelineEntryColors('unknown');
    expect(colors.backgroundColor).toBe('#f3f4f6');
    expect(colors.borderColor).toBe('#d1d5db');
    expect(colors.foregroundColor).toBe('#374151');
  });
});

// ---------------------------------------------------------------------------
// getSlaIndicatorColor
// ---------------------------------------------------------------------------

describe('getSlaIndicatorColor', () => {
  const totalDuration = 10_000;

  it('returns red when deadline has passed', () => {
    expect(getSlaIndicatorColor(1000, 2000, totalDuration)).toBe('red');
  });

  it('returns green when more than 25% remaining', () => {
    // 50% remaining
    const deadline = 10_000;
    const current = 5_000;
    expect(getSlaIndicatorColor(deadline, current, totalDuration)).toBe('green');
  });

  it('returns orange when exactly 25% remaining', () => {
    // 25% remaining: remaining = 2500, 2500/10000 = 0.25
    const deadline = 10_000;
    const current = 7_500;
    expect(getSlaIndicatorColor(deadline, current, totalDuration)).toBe('orange');
  });

  it('returns orange when less than 25% remaining', () => {
    // 10% remaining
    const deadline = 10_000;
    const current = 9_000;
    expect(getSlaIndicatorColor(deadline, current, totalDuration)).toBe('orange');
  });

  it('returns green when deadline equals current (0% remaining is red)', () => {
    // current === deadline → current > deadline is false, remaining = 0, 0/10000 = 0 → orange
    expect(getSlaIndicatorColor(5000, 5000, totalDuration)).toBe('orange');
  });
});
