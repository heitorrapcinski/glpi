import { useNavigate } from 'react-router-dom';
import SearchEngine, {
  type ColumnDef,
  type FilterDef,
  type BulkActionDef,
} from '@/components/common/SearchEngine';
import StatusBadge from '@/components/common/StatusBadge';
import PriorityBadge from '@/components/common/PriorityBadge';
import { PROBLEMS } from '@/api/endpoints';
import { formatDate } from '@/utils/formatters';
import type { Problem, Actor } from '@/hooks/useProblems';

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function getActorName(actors: Actor[], actorType: number): string {
  const actor = actors.find((a) => a.actorType === actorType);
  return actor?.displayName ?? actor?.actorId ?? '—';
}

// ---------------------------------------------------------------------------
// Column definitions
// ---------------------------------------------------------------------------

const columns: ColumnDef<Problem>[] = [
  {
    key: 'id',
    label: 'ID',
    sortable: true,
    width: '70px',
  },
  {
    key: 'status',
    label: 'Status',
    sortable: true,
    width: '120px',
    render: (value) => <StatusBadge code={value as number} size="sm" />,
  },
  {
    key: 'title',
    label: 'Title',
    sortable: true,
    render: (value) => (
      <span style={{ fontWeight: 500 }}>{value as string}</span>
    ),
  },
  {
    key: 'priority',
    label: 'Priority',
    sortable: true,
    width: '120px',
    render: (value) => <PriorityBadge code={value as number} size="sm" />,
  },
  {
    key: 'actors',
    label: 'Assigned',
    sortable: false,
    width: '140px',
    render: (_value, item) => getActorName(item.actors, 2),
  },
  {
    key: 'entityId',
    label: 'Entity',
    sortable: true,
    width: '120px',
    render: (value) => (value as string) || '—',
  },
  {
    key: 'linkedTicketIds',
    label: 'Linked Tickets',
    sortable: false,
    width: '110px',
    render: (value) => {
      const ids = value as string[];
      return ids.length > 0 ? (
        <span
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            minWidth: '22px',
            height: '22px',
            padding: '0 6px',
            borderRadius: '11px',
            backgroundColor: 'var(--tblr-bg-surface-secondary, #f5f7fb)',
            border: '1px solid var(--tblr-border-color, #d9dbde)',
            fontSize: '0.75rem',
            fontWeight: 600,
          }}
        >
          {ids.length}
        </span>
      ) : (
        <span style={{ color: 'var(--tblr-secondary, #606f91)' }}>—</span>
      );
    },
  },
  {
    key: 'updatedAt',
    label: 'Last Update',
    sortable: true,
    width: '160px',
    render: (value) => (value ? formatDate(value as string) : '—'),
  },
];

// ---------------------------------------------------------------------------
// Filter definitions
// ---------------------------------------------------------------------------

const filters: FilterDef[] = [
  {
    key: 'status',
    label: 'Status',
    type: 'select',
    options: [
      { value: '1', label: 'New' },
      { value: '2', label: 'Assigned' },
      { value: '3', label: 'Planned' },
      { value: '4', label: 'Pending' },
      { value: '5', label: 'Solved' },
      { value: '6', label: 'Closed' },
    ],
  },
  {
    key: 'priority',
    label: 'Priority',
    type: 'select',
    options: [
      { value: '1', label: 'Very Low' },
      { value: '2', label: 'Low' },
      { value: '3', label: 'Medium' },
      { value: '4', label: 'High' },
      { value: '5', label: 'Very High' },
      { value: '6', label: 'Major' },
    ],
  },
  {
    key: 'entityId',
    label: 'Entity',
    type: 'text',
  },
  {
    key: 'assignedUserId',
    label: 'Assigned User',
    type: 'text',
  },
];

// ---------------------------------------------------------------------------
// ProblemListPage
// ---------------------------------------------------------------------------

export default function ProblemListPage() {
  const navigate = useNavigate();

  const bulkActions: BulkActionDef[] = [
    {
      key: 'change-status',
      label: 'Change Status',
      onAction: (ids) => {
        console.info('Change status for problems:', ids);
      },
    },
    {
      key: 'assign',
      label: 'Assign',
      onAction: (ids) => {
        console.info('Assign problems:', ids);
      },
    },
    {
      key: 'delete',
      label: 'Delete',
      variant: 'danger',
      onAction: (ids) => {
        console.info('Delete problems:', ids);
      },
    },
  ];

  return (
    <main>
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          marginBottom: '1rem',
        }}
      >
        <h1
          style={{
            fontSize: '1.25rem',
            fontWeight: 700,
            margin: 0,
            color: 'var(--tblr-body-color, #1e293b)',
          }}
        >
          Problems
        </h1>
        <button
          type="button"
          onClick={() => navigate('/problems/new')}
          style={{
            padding: '0.4rem 1rem',
            background: 'var(--tblr-primary, rgb(254,201,92))',
            color: 'var(--tblr-primary-fg, #1e293b)',
            border: 'none',
            borderRadius: '4px',
            fontWeight: 600,
            fontSize: '0.8125rem',
            cursor: 'pointer',
            minHeight: '36px',
          }}
        >
          + New Problem
        </button>
      </div>

      <SearchEngine<Problem>
        endpoint={PROBLEMS.LIST}
        columns={columns}
        defaultSort={{ field: 'updatedAt', order: 'desc' }}
        filters={filters}
        bulkActions={bulkActions}
        onRowClick={(problem) => navigate(`/problems/${problem.id}`)}
        pageSize={50}
      />
    </main>
  );
}
