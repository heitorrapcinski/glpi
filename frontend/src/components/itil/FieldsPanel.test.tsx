import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import FieldsPanel from './FieldsPanel';
import type { FieldsPanelProps } from './FieldsPanel';
import type { Actor, SlaContext } from '@/hooks/useTickets';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

const baseActors: Actor[] = [
  { actorType: 1, actorKind: 'user', actorId: 'u1', useNotification: true, displayName: 'Alice Requester' },
  { actorType: 2, actorKind: 'user', actorId: 'u2', useNotification: true, displayName: 'Bob Assigned' },
  { actorType: 3, actorKind: 'user', actorId: 'u3', useNotification: false, displayName: 'Carol Observer' },
];

const baseDates = {
  createdAt: '2025-01-15T10:00:00Z',
  updatedAt: '2025-01-16T14:30:00Z',
  solvedAt: null,
  closedAt: null,
};

function makeProps(overrides: Partial<FieldsPanelProps> = {}): FieldsPanelProps {
  return {
    status: 1,
    priority: 3,
    urgency: 3,
    impact: 2,
    categoryId: 'cat-1',
    actors: baseActors,
    sla: null,
    dates: baseDates,
    linkedItems: [],
    onUpdate: vi.fn(),
    collapsed: false,
    onToggleCollapse: vi.fn(),
    ...overrides,
  };
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe('FieldsPanel', () => {
  it('renders expanded panel with status section open by default', () => {
    render(<FieldsPanel {...makeProps()} />);
    expect(screen.getByTestId('fields-panel')).toBeTruthy();
    expect(screen.getByLabelText('Status: New')).toBeTruthy();
  });

  it('renders collapsed panel with icon strip', () => {
    render(<FieldsPanel {...makeProps({ collapsed: true })} />);
    expect(screen.getByTestId('fields-panel-collapsed')).toBeTruthy();
    expect(screen.getByLabelText('Expand fields panel')).toBeTruthy();
    expect(screen.getByTitle('Status')).toBeTruthy();
    expect(screen.getByTitle('Dates')).toBeTruthy();
    expect(screen.getByTitle('Actors')).toBeTruthy();
  });

  it('calls onToggleCollapse when toggle button is clicked (expanded)', () => {
    const onToggle = vi.fn();
    render(<FieldsPanel {...makeProps({ onToggleCollapse: onToggle })} />);
    fireEvent.click(screen.getByLabelText('Collapse fields panel'));
    expect(onToggle).toHaveBeenCalledOnce();
  });

  it('calls onToggleCollapse when toggle button is clicked (collapsed)', () => {
    const onToggle = vi.fn();
    render(<FieldsPanel {...makeProps({ collapsed: true, onToggleCollapse: onToggle })} />);
    fireEvent.click(screen.getByLabelText('Expand fields panel'));
    expect(onToggle).toHaveBeenCalledOnce();
  });

  it('calls onUpdate when status is changed', () => {
    const onUpdate = vi.fn();
    render(<FieldsPanel {...makeProps({ onUpdate })} />);
    const select = screen.getByLabelText('Change status');
    fireEvent.change(select, { target: { value: '5' } });
    expect(onUpdate).toHaveBeenCalledWith('status', 5);
  });

  it('renders actors grouped by role', () => {
    render(<FieldsPanel {...makeProps()} />);
    // Actors section is open by default — ActorBadge uses title and aria-label
    expect(screen.getByLabelText('Alice Requester')).toBeTruthy();
    expect(screen.getByLabelText('Bob Assigned')).toBeTruthy();
    expect(screen.getByLabelText('Carol Observer')).toBeTruthy();
  });

  it('expands accordion section on click', () => {
    render(<FieldsPanel {...makeProps()} />);
    // Dates section is closed by default — click to open
    const datesHeader = screen.getByText('Dates');
    fireEvent.click(datesHeader);
    expect(screen.getByText('Created')).toBeTruthy();
  });

  it('calls onUpdate when urgency is changed', () => {
    const onUpdate = vi.fn();
    render(<FieldsPanel {...makeProps({ onUpdate })} />);
    // Open priority section
    fireEvent.click(screen.getByText('Priority / Urgency / Impact'));
    const select = screen.getByLabelText('Change urgency');
    fireEvent.change(select, { target: { value: '4' } });
    expect(onUpdate).toHaveBeenCalledWith('urgency', 4);
  });

  it('calls onUpdate when impact is changed', () => {
    const onUpdate = vi.fn();
    render(<FieldsPanel {...makeProps({ onUpdate })} />);
    fireEvent.click(screen.getByText('Priority / Urgency / Impact'));
    const select = screen.getByLabelText('Change impact');
    fireEvent.change(select, { target: { value: '3' } });
    expect(onUpdate).toHaveBeenCalledWith('impact', 3);
  });

  it('displays SLA deadline with color indicator', () => {
    // Deadline far in the future → green
    const futureSla: SlaContext = {
      deadline: new Date(Date.now() + 86400000 * 10).toISOString(),
      totalDuration: 86400000 * 14,
    };
    render(<FieldsPanel {...makeProps({ sla: futureSla })} />);
    fireEvent.click(screen.getByText('SLA'));
    const indicator = screen.getByLabelText(/SLA deadline: green/);
    expect(indicator).toBeTruthy();
  });

  it('displays SLA deadline as red when breached', () => {
    const pastSla: SlaContext = {
      deadline: new Date(Date.now() - 86400000).toISOString(),
      totalDuration: 86400000 * 7,
    };
    render(<FieldsPanel {...makeProps({ sla: pastSla })} />);
    fireEvent.click(screen.getByText('SLA'));
    const indicator = screen.getByLabelText(/SLA deadline: red/);
    expect(indicator).toBeTruthy();
  });

  it('renders linked items', () => {
    const items = [
      { type: 'ticket', id: '42', title: 'Related ticket' },
      { type: 'problem', id: '7', title: 'Root cause' },
    ];
    render(<FieldsPanel {...makeProps({ linkedItems: items })} />);
    fireEvent.click(screen.getByText('Linked Items'));
    expect(screen.getByText('#42')).toBeTruthy();
    expect(screen.getByText('Related ticket')).toBeTruthy();
    expect(screen.getByText('#7')).toBeTruthy();
  });

  it('renders extra sections for problems/changes', () => {
    const extra = [
      { key: 'impact-analysis', label: 'Impact Analysis', icon: '📊', content: <div>Impact content here</div> },
    ];
    render(<FieldsPanel {...makeProps({ extraSections: extra })} />);
    fireEvent.click(screen.getByText('Impact Analysis'));
    expect(screen.getByText('Impact content here')).toBeTruthy();
  });

  it('shows extra section icons in collapsed mode', () => {
    const extra = [
      { key: 'root-cause', label: 'Root Cause', icon: '🔍', content: <div>Root cause</div> },
    ];
    render(<FieldsPanel {...makeProps({ collapsed: true, extraSections: extra })} />);
    expect(screen.getByTitle('Root Cause')).toBeTruthy();
  });
});
