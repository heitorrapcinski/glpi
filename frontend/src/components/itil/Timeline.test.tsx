import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Timeline from './Timeline';
import type { TimelineEntry as TEntry } from '@/hooks/useTickets';

// ---------------------------------------------------------------------------
// Fixtures
// ---------------------------------------------------------------------------

const followupEntry: TEntry = {
  type: 'followup',
  data: {
    id: 'f1',
    content: '<p>Followup content</p>',
    authorId: 'u1',
    authorName: 'Alice',
    isPrivate: false,
    source: 'web',
    createdAt: '2025-01-15T10:00:00Z',
  },
};

const taskEntry: TEntry = {
  type: 'task',
  data: {
    id: 't1',
    content: '<p>Task content</p>',
    assignedUserId: 'u2',
    assignedUserName: 'Bob',
    status: 1,
    isPrivate: false,
    plannedStart: null,
    plannedEnd: null,
    duration: 0,
    createdAt: '2025-01-15T12:00:00Z',
  },
};

const pendingSolution: TEntry = {
  type: 'solution',
  data: {
    id: 's1',
    content: '<p>Solution content</p>',
    authorId: 'u3',
    authorName: 'Charlie',
    status: 'pending',
    createdAt: '2025-01-15T14:00:00Z',
  },
};

const acceptedSolution: TEntry = {
  type: 'solution',
  data: {
    id: 's2',
    content: '<p>Accepted solution</p>',
    authorId: 'u3',
    authorName: 'Charlie',
    status: 'accepted',
    createdAt: '2025-01-15T15:00:00Z',
  },
};

const documentEntry: TEntry = {
  type: 'document',
  data: {
    id: 'd1',
    filename: 'screenshot.png',
    filepath: '/uploads/screenshot.png',
    mime: 'image/png',
    authorId: 'u1',
    authorName: 'Alice',
    createdAt: '2025-01-15T11:00:00Z',
  },
};

const logEntry: TEntry = {
  type: 'log',
  data: {
    id: 'l1',
    message: 'Status changed to Assigned',
    createdAt: '2025-01-15T09:00:00Z',
  },
};

const allEntries: TEntry[] = [
  followupEntry,
  taskEntry,
  pendingSolution,
  documentEntry,
  logEntry,
];

const noop = () => {};

// ---------------------------------------------------------------------------
// Tests — Timeline
// ---------------------------------------------------------------------------

describe('Timeline', () => {
  it('renders all entries', () => {
    render(
      <Timeline
        entries={allEntries}
        order="oldest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    expect(screen.getByText('Followup content')).toBeInTheDocument();
    expect(screen.getByText('Task content')).toBeInTheDocument();
    expect(screen.getByText('Solution content')).toBeInTheDocument();
    expect(screen.getByText('screenshot.png')).toBeInTheDocument();
    expect(screen.getByText('Status changed to Assigned')).toBeInTheDocument();
  });

  it('shows empty state when no entries', () => {
    render(
      <Timeline
        entries={[]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    expect(screen.getByText('No timeline entries yet.')).toBeInTheDocument();
  });

  it('sorts entries newest first', () => {
    render(
      <Timeline
        entries={allEntries}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    const articles = screen.getAllByRole('article');
    // Newest first: solution (14:00) > task (12:00) > document (11:00) > followup (10:00) > log (09:00)
    expect(articles[0]).toHaveAttribute('aria-label', 'Solution entry');
    expect(articles[articles.length - 1]).toHaveAttribute('aria-label', 'Log entry');
  });

  it('sorts entries oldest first', () => {
    render(
      <Timeline
        entries={allEntries}
        order="oldest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    const articles = screen.getAllByRole('article');
    expect(articles[0]).toHaveAttribute('aria-label', 'Log entry');
    expect(articles[articles.length - 1]).toHaveAttribute('aria-label', 'Solution entry');
  });

  it('renders all four action buttons', () => {
    render(
      <Timeline
        entries={[]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    expect(screen.getByRole('button', { name: 'Add Followup' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add Task' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add Solution' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Add Document' })).toBeInTheDocument();
  });

  it('toggles form placeholder when action button is clicked', () => {
    render(
      <Timeline
        entries={[]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    const btn = screen.getByRole('button', { name: 'Add Followup' });
    fireEvent.click(btn);
    expect(screen.getByText(/Followup form placeholder/)).toBeInTheDocument();

    // Click again to close
    fireEvent.click(btn);
    expect(screen.queryByText(/Followup form placeholder/)).not.toBeInTheDocument();
  });

  it('switches form when a different action button is clicked', () => {
    render(
      <Timeline
        entries={[]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Add Followup' }));
    expect(screen.getByText(/Followup form placeholder/)).toBeInTheDocument();

    fireEvent.click(screen.getByRole('button', { name: 'Add Task' }));
    expect(screen.queryByText(/Followup form placeholder/)).not.toBeInTheDocument();
    expect(screen.getByText(/Task form placeholder/)).toBeInTheDocument();
  });
});

// ---------------------------------------------------------------------------
// Tests — TimelineEntry
// ---------------------------------------------------------------------------

describe('TimelineEntry (via Timeline)', () => {
  it('shows approve/reject buttons on pending solution when canApprove', () => {
    render(
      <Timeline
        entries={[pendingSolution]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
        canApprove
        onApproveSolution={noop}
        onRejectSolution={noop}
      />,
    );

    expect(screen.getByRole('button', { name: 'Approve solution' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Reject solution' })).toBeInTheDocument();
  });

  it('does not show approve/reject on accepted solution', () => {
    render(
      <Timeline
        entries={[acceptedSolution]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
        canApprove
      />,
    );

    expect(screen.queryByRole('button', { name: 'Approve solution' })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Reject solution' })).not.toBeInTheDocument();
  });

  it('does not show approve/reject when canApprove is false', () => {
    render(
      <Timeline
        entries={[pendingSolution]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
        canApprove={false}
      />,
    );

    expect(screen.queryByRole('button', { name: 'Approve solution' })).not.toBeInTheDocument();
  });

  it('calls onApproveSolution with solution id', () => {
    const onApprove = vi.fn();
    render(
      <Timeline
        entries={[pendingSolution]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
        canApprove
        onApproveSolution={onApprove}
        onRejectSolution={noop}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Approve solution' }));
    expect(onApprove).toHaveBeenCalledWith('s1');
  });

  it('calls onRejectSolution with solution id', () => {
    const onReject = vi.fn();
    render(
      <Timeline
        entries={[pendingSolution]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
        canApprove
        onApproveSolution={noop}
        onRejectSolution={onReject}
      />,
    );

    fireEvent.click(screen.getByRole('button', { name: 'Reject solution' }));
    expect(onReject).toHaveBeenCalledWith('s1');
  });

  it('displays author name and type badge', () => {
    render(
      <Timeline
        entries={[followupEntry]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    expect(screen.getByText('Alice')).toBeInTheDocument();
    expect(screen.getByText('Followup')).toBeInTheDocument();
  });

  it('renders document entry as plain text (not HTML)', () => {
    render(
      <Timeline
        entries={[documentEntry]}
        order="newest"
        onAddFollowup={noop}
        onAddTask={noop}
        onAddSolution={noop}
      />,
    );

    expect(screen.getByText('screenshot.png')).toBeInTheDocument();
  });
});
