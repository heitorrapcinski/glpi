import { useNavigate } from 'react-router-dom';
import SearchEngine, {
  type ColumnDef,
  type FilterDef,
  type BulkActionDef,
} from '@/components/common/SearchEngine';
import StatusBadge from '@/components/common/StatusBadge';
import PriorityBadge from '@/components/common/PriorityBadge';
import { CHANGES } from '@/api/endpoints';
import { formatDate } from '@/utils/formatters';
import type { Change } from '@/hooks/useChanges';
import type { Actor } from '@/hooks/useTickets';

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

const columns: ColumnDef<Change>[] = [
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
// ChangeListPage
// ---------------------------------------------------------------------------

export default function ChangeListPage() {
  const navigate = useNavigate();

  const bulkActions: BulkActionDef[] = [
    {
      key: 'change-status',
      label: 'Change Status',
      onAction: (ids) => {
        console.info('Change status for changes:', ids);
      },
    },
    {
      key: 'assign',
      label: 'Assign',
      onAction: (ids) => {
        console.info('Assign changes:', ids);
      },
    },
    {
      key: 'delete',
      label: 'Delete',
      variant: 'danger',
      onAction: (ids) => {
        console.info('Delete changes:', ids);
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
          Changes
        </h1>
        <button
          type="button"
          onClick={() => navigate('/changes/new')}
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
          + New Change
        </button>
      </div>

      <SearchEngine<Change>
        endpoint={CHANGES.LIST}
        columns={columns}
        defaultSort={{ field: 'updatedAt', order: 'desc' }}
        filters={filters}
        bulkActions={bulkActions}
        onRowClick={(change) => navigate(`/changes/${change.id}`)}
        pageSize={50}
      />
    </main>
  );
}
